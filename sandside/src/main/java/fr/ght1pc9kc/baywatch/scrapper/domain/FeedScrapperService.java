package fr.ght1pc9kc.baywatch.scrapper.domain;

import fr.ght1pc9kc.baywatch.common.domain.DateUtils;
import fr.ght1pc9kc.baywatch.scrapper.api.FeedScrapperPlugin;
import fr.ght1pc9kc.baywatch.scrapper.api.NewsFilter;
import fr.ght1pc9kc.baywatch.scrapper.api.RssAtomParser;
import fr.ght1pc9kc.baywatch.scrapper.api.ScrappingHandler;
import fr.ght1pc9kc.baywatch.scrapper.infra.config.ScraperProperties;
import fr.ght1pc9kc.baywatch.security.api.AuthenticationFacade;
import fr.ght1pc9kc.baywatch.techwatch.api.model.Feed;
import fr.ght1pc9kc.baywatch.techwatch.api.model.News;
import fr.ght1pc9kc.baywatch.techwatch.api.model.RawNews;
import fr.ght1pc9kc.baywatch.techwatch.api.model.State;
import fr.ght1pc9kc.baywatch.techwatch.domain.ports.FeedPersistencePort;
import fr.ght1pc9kc.baywatch.techwatch.domain.ports.NewsPersistencePort;
import lombok.AccessLevel;
import lombok.Setter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.VisibleForTesting;
import org.springframework.core.ResolvableType;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.MediaType;
import org.springframework.http.codec.xml.XmlEventDecoder;
import org.springframework.scheduling.concurrent.CustomizableThreadFactory;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
public final class FeedScrapperService implements Runnable {

    private static final Set<String> SUPPORTED_SCHEMES = Set.of("http", "https");
    public static final String ERROR_CLASS_MESSAGE = "{}: {}";
    public static final String ERROR_STACKTRACE_MESSAGE = "STACKTRACE";

    private final ScheduledExecutorService scheduleExecutor = Executors.newSingleThreadScheduledExecutor(
            new CustomizableThreadFactory("scrapSched-"));
    private final Scheduler scraperScheduler =
            Schedulers.newBoundedElastic(5, Integer.MAX_VALUE, "scrapper");
    private final Semaphore lock = new Semaphore(1);
    private final WebClient http;

    private final ScraperProperties properties;
    private final FeedPersistencePort feedRepository;
    private final NewsPersistencePort newsRepository;
    private final RssAtomParser feedParser;
    private final Collection<ScrappingHandler> scrappingHandlers;
    private final Map<String, FeedScrapperPlugin> plugins;
    private final List<NewsFilter> newsFilters;
    private final XmlEventDecoder xmlEventDecoder;

    @Setter(value = AccessLevel.PACKAGE, onMethod = @__({@VisibleForTesting}))
    private Clock clock = Clock.systemUTC();

    public FeedScrapperService(ScraperProperties properties,
                               FeedPersistencePort feedRepository, NewsPersistencePort newsRepository,
                               WebClient webClient, RssAtomParser feedParser,
                               Collection<ScrappingHandler> scrappingHandlers,
                               Map<String, FeedScrapperPlugin> plugins,
                               List<NewsFilter> newsFilters
    ) {
        this.properties = properties;
        this.feedRepository = feedRepository;
        this.newsRepository = newsRepository;
        this.feedParser = feedParser;
        this.scrappingHandlers = scrappingHandlers;
        this.plugins = plugins;
        this.newsFilters = newsFilters;
        this.http = webClient;

        this.xmlEventDecoder = new XmlEventDecoder();
        this.xmlEventDecoder.setMaxInMemorySize(16 * 1024 * 1024);
    }

    public void startScrapping() {
        Instant now = clock.instant();
        Instant nextScrapping = now.plus(properties.frequency());
        Duration toNextScrapping = Duration.between(now, nextScrapping);

        scheduleExecutor.scheduleAtFixedRate(this,
                toNextScrapping.getSeconds(), properties.frequency().getSeconds(), TimeUnit.SECONDS);
        log.debug("Next scraping at {}", LocalDateTime.now(clock).plus(toNextScrapping));
        scheduleExecutor.schedule(this, 0, TimeUnit.MILLISECONDS);
    }

    @SneakyThrows
    public void shutdownScrapping() {
        if (!lock.tryAcquire(60, TimeUnit.SECONDS)) {
            log.warn("Unable to stop threads gracefully ! Threads was killed !");
        }
        scraperScheduler.dispose();
        scheduleExecutor.shutdownNow();
        lock.release();
        log.info("All scraper tasks finished and stopped !");
    }

    @Override
    @SneakyThrows
    public void run() {
        if (!lock.tryAcquire()) {
            log.warn("Scraping in progress !");
            return;
        }
        log.info("Start scraping ...");
        Mono<Set<String>> alreadyHave = newsRepository.list()
                .map(News::getId)
                .collect(Collectors.toUnmodifiableSet())
                .cache();

        Flux.concat(scrappingHandlers.stream().map(ScrappingHandler::before).toList())
                .thenMany(feedRepository.list())
                .parallel(4).runOn(scraperScheduler)
                .concatMap(this::wgetFeedNews)
                .sequential()

                .filterWhen(n -> alreadyHave.map(l -> !l.contains(n.getId())))
                .parallel(4).runOn(scraperScheduler)
                .concatMap(this::applyNewsFilters)
                .sequential()

                .filterWhen(n -> alreadyHave.map(l -> !l.contains(n.getId())))
                .buffer(100)
                .flatMap(newsRepository::persist)

                .reduce(Integer::sum)
                .flatMap(count -> Flux.concat(scrappingHandlers.stream().map(h -> h.after(count)).toList()).then())
                .doOnError(e -> {
                    log.error(ERROR_CLASS_MESSAGE, e.getClass(), e.getLocalizedMessage());
                    log.debug(ERROR_STACKTRACE_MESSAGE, e);
                })
                .doFinally(signal -> {
                    lock.release();
                    log.info("Scraping terminated successfully !");
                })
                .contextWrite(AuthenticationFacade.withSystemAuthentication())
                .subscribe();
    }

    private Flux<News> wgetFeedNews(Feed feed) {
        String feedHost = feed.getUrl().getHost();
        FeedScrapperPlugin hostPlugin = plugins.get(feedHost);
        URI feedUrl = (hostPlugin != null) ? hostPlugin.uriModifier(feed.getUrl()) : feed.getUrl();

        if (!SUPPORTED_SCHEMES.contains(feedUrl.getScheme())) {
            log.warn("Unsupported scheme for {} !", feedUrl);
            return Flux.empty();
        }

        log.debug("Start scraping feed {} ...", feedHost);

        final Instant maxAge = LocalDate.now(clock)
                .minus(properties.conservation())
                .atTime(LocalTime.MAX)
                .toInstant(DateUtils.DEFAULT_ZONE_OFFSET);

        return http.get()
                .uri(feedUrl)
                .accept(MediaType.APPLICATION_ATOM_XML)
                .accept(MediaType.APPLICATION_RSS_XML)
                .acceptCharset(StandardCharsets.UTF_8)
                .exchangeToFlux(response -> {
                    if (!response.statusCode().is2xxSuccessful()) {
                        log.info("Host {} respond {}", feedHost, response.statusCode());
                        return Flux.empty();
                    }
                    return this.xmlEventDecoder.decode(
                            response.bodyToFlux(DataBuffer.class),
                            ResolvableType.NONE, null, null);
                })
                .doFirst(() -> log.trace("Receiving event from {}...", feedHost))

                .skipUntil(feedParser.firstItemEvent())
                .bufferUntil(feedParser.itemEndEvent())
                .flatMap(entry -> feedParser.readEntryEvents(entry, feed))
                .takeWhile(news -> news.getPublication().isAfter(maxAge))
                .map(raw -> News.builder()
                        .raw(raw)
                        .feeds(Set.of(feed.getId()))
                        .state(State.NONE)
                        .build())

                .doFinally(s -> log.debug("Finish scraping feed {}", feedHost))
                .onErrorResume(e -> {
                    log.warn(ERROR_CLASS_MESSAGE, feedHost, e.getLocalizedMessage());
                    log.debug(ERROR_STACKTRACE_MESSAGE, e);
                    return Flux.empty();
                });
    }

    private Mono<News> applyNewsFilters(News news) {
        Mono<RawNews> raw = Mono.just(news.getRaw());
        for (NewsFilter filter : newsFilters) {
            raw = raw.flatMap(filter::filter);
        }
        return raw.map(news::withRaw);
    }

    @VisibleForTesting
    public boolean isScraping() {
        return lock.availablePermits() == 0;
    }
}
