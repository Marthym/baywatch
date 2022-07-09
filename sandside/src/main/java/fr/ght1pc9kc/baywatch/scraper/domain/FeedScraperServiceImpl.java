package fr.ght1pc9kc.baywatch.scraper.domain;

import fr.ght1pc9kc.baywatch.common.domain.DateUtils;
import fr.ght1pc9kc.baywatch.scraper.api.FeedScraperPlugin;
import fr.ght1pc9kc.baywatch.scraper.api.FeedScraperService;
import fr.ght1pc9kc.baywatch.scraper.api.NewsEnrichmentService;
import fr.ght1pc9kc.baywatch.scraper.api.RssAtomParser;
import fr.ght1pc9kc.baywatch.scraper.api.ScrapingHandler;
import fr.ght1pc9kc.baywatch.scraper.api.model.AtomFeed;
import fr.ght1pc9kc.baywatch.scraper.domain.model.ScrapedFeed;
import fr.ght1pc9kc.baywatch.scraper.domain.model.ScraperConfig;
import fr.ght1pc9kc.baywatch.scraper.domain.ports.NewsMaintenancePort;
import fr.ght1pc9kc.baywatch.security.api.AuthenticationFacade;
import fr.ght1pc9kc.baywatch.techwatch.api.model.News;
import fr.ght1pc9kc.baywatch.techwatch.api.model.State;
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
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
public final class FeedScraperServiceImpl implements Runnable, FeedScraperService {

    private static final Set<String> SUPPORTED_SCHEMES = Set.of("http", "https");
    public static final String ERROR_CLASS_MESSAGE = "{}: {}";
    public static final String ERROR_STACKTRACE_MESSAGE = "STACKTRACE";

    private final ScheduledExecutorService scheduleExecutor = Executors.newSingleThreadScheduledExecutor(
            new CustomizableThreadFactory("scrapSched-"));
    private final Scheduler scraperScheduler =
            Schedulers.newBoundedElastic(5, Integer.MAX_VALUE, "scrapper");
    private final Semaphore lock = new Semaphore(1);
    private final WebClient http;

    private final ScraperConfig properties;
    private final NewsMaintenancePort newsMaintenance;
    private final RssAtomParser feedParser;
    private final Collection<ScrapingHandler> scrapingHandlers;
    private final Map<String, FeedScraperPlugin> plugins;
    private final NewsEnrichmentService newsEnrichmentService;
    private final XmlEventDecoder xmlEventDecoder;

    @Setter(value = AccessLevel.PACKAGE, onMethod = @__({@VisibleForTesting}))
    private Clock clock = Clock.systemUTC();

    public FeedScraperServiceImpl(ScraperConfig properties,
                                  NewsMaintenancePort newsMaintenance,
                                  WebClient webClient, RssAtomParser feedParser,
                                  Collection<ScrapingHandler> scrapingHandlers,
                                  Map<String, FeedScraperPlugin> plugins,
                                  NewsEnrichmentService newsEnrichmentService
    ) {
        this.properties = properties;
        this.newsMaintenance = newsMaintenance;
        this.feedParser = feedParser;
        this.scrapingHandlers = scrapingHandlers;
        this.plugins = plugins;
        this.newsEnrichmentService = newsEnrichmentService;
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
    public void run() {
        try {
            if (!lock.tryAcquire()) {
                log.warn("Scraping in progress !");
                return;
            }
            log.info("Start scraping ...");
            Mono<Set<String>> alreadyHave = newsMaintenance.listAllNewsId()
                    .collect(Collectors.toUnmodifiableSet())
                    .cache();

            Flux.concat(scrapingHandlers.stream().map(ScrapingHandler::before).toList())
                    .thenMany(newsMaintenance.feedList())
                    .parallel(4).runOn(scraperScheduler)
                    .concatMap(this::wgetFeedNews)
                    .sequential()

                    .filterWhen(n -> alreadyHave.map(l -> !l.contains(n.getId())))
                    .parallel(4).runOn(scraperScheduler)
                    .concatMap(newsEnrichmentService::applyNewsFilters)
                    .sequential()

                    .filterWhen(n -> alreadyHave.map(l -> !l.contains(n.getId())))
                    .buffer(100)
                    .flatMap(newsMaintenance::newsLoad)

                    .reduce(Integer::sum)
                    .flatMap(count -> Flux.concat(scrapingHandlers.stream().map(h -> h.after(count)).toList()).then())
                    .doOnError(e -> {
                        log.error(ERROR_CLASS_MESSAGE, e.getClass(), e.getLocalizedMessage());
                        log.debug(ERROR_STACKTRACE_MESSAGE, e);
                    })
                    .doFinally(signal -> {
                        lock.release();
                        scrapingHandlers.forEach(ScrapingHandler::onTerminate);
                        log.info("Scraping terminated successfully !");
                    })
                    .contextWrite(AuthenticationFacade.withSystemAuthentication())
                    .subscribe();
        } catch (Exception e) {
            lock.release();
            log.error("Scraping terminated on error !");
            log.error(ERROR_CLASS_MESSAGE, e.getClass(), e.getLocalizedMessage());
            log.debug(ERROR_STACKTRACE_MESSAGE, e);
        }
    }

    private Flux<News> wgetFeedNews(ScrapedFeed feed) {
        String feedHost = feed.link().getHost();
        FeedScraperPlugin hostPlugin = plugins.get(feedHost);
        URI feedUrl = (hostPlugin != null) ? hostPlugin.uriModifier(feed.link()) : feed.link();

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
                        .feeds(Set.of(feed.id()))
                        .state(State.NONE)
                        .build())

                .doFinally(s -> log.debug("Finish scraping feed {}", feedHost))
                .onErrorResume(e -> {
                    log.warn(ERROR_CLASS_MESSAGE, feedHost, e.getLocalizedMessage());
                    log.debug(ERROR_STACKTRACE_MESSAGE, e);
                    return Flux.empty();
                });
    }

    @VisibleForTesting
    public boolean isScraping() {
        return lock.availablePermits() == 0;
    }

    @Override
    public Mono<AtomFeed> scrapFeedHeader(URI link) {
        return http.get()
                .uri(link)
                .accept(MediaType.APPLICATION_ATOM_XML)
                .accept(MediaType.APPLICATION_RSS_XML)
                .acceptCharset(StandardCharsets.UTF_8)
                .exchangeToFlux(response -> {
                    if (!response.statusCode().is2xxSuccessful()) {
                        log.info("Host {} respond {}", link.getHost(), response.statusCode());
                        return Flux.empty();
                    }
                    return this.xmlEventDecoder.decode(
                            response.bodyToFlux(DataBuffer.class),
                            ResolvableType.NONE, null, null);
                })

                .bufferUntil(feedParser.firstItemEvent())
                .next()
                .map(feedParser::readFeedProperties);
    }
}
