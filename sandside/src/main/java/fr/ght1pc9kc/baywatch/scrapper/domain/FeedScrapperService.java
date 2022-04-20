package fr.ght1pc9kc.baywatch.scrapper.domain;

import com.machinezoo.noexception.Exceptions;
import fr.ght1pc9kc.baywatch.scrapper.api.FeedScrapperPlugin;
import fr.ght1pc9kc.baywatch.scrapper.api.RssAtomParser;
import fr.ght1pc9kc.baywatch.scrapper.api.ScrappingHandler;
import fr.ght1pc9kc.baywatch.security.api.AuthenticationFacade;
import fr.ght1pc9kc.baywatch.techwatch.api.model.Feed;
import fr.ght1pc9kc.baywatch.techwatch.api.model.News;
import fr.ght1pc9kc.baywatch.techwatch.api.model.RawNews;
import fr.ght1pc9kc.baywatch.techwatch.domain.ports.FeedPersistencePort;
import fr.ght1pc9kc.baywatch.techwatch.domain.ports.NewsPersistencePort;
import fr.ght1pc9kc.scraphead.core.HeadScraper;
import fr.ght1pc9kc.scraphead.core.model.links.Links;
import fr.ght1pc9kc.scraphead.core.model.opengraph.OpenGraph;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.VisibleForTesting;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.scheduling.concurrent.CustomizableThreadFactory;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.Disposable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;
import reactor.netty.http.client.HttpClient;

import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static java.util.Objects.nonNull;

@Slf4j
@RequiredArgsConstructor
public final class FeedScrapperService implements Runnable {

    private static final Set<String> SUPPORTED_SCHEMES = Set.of("http", "https");
    public static final String ERROR_CLASS_MESSAGE = "{}: {}";
    public static final String ERROR_STACKTRACE_MESSAGE = "STACKTRACE";

    private final ScheduledExecutorService scheduleExecutor = Executors.newSingleThreadScheduledExecutor(
            new CustomizableThreadFactory("scrapSched-"));
    private final Scheduler scrapperScheduler =
            Schedulers.newBoundedElastic(5, Integer.MAX_VALUE, "scrapper");
    private final WebClient http = WebClient.builder()
            .clientConnector(new ReactorClientHttpConnector(
                    HttpClient.create()
                            .followRedirect(true)
                            .compress(true)
            )).build();
    private final Clock clock = Clock.systemUTC();
    private final Semaphore lock = new Semaphore(1);

    private final Duration scrapFrequency;
    private final FeedPersistencePort feedRepository;
    private final NewsPersistencePort newsRepository;
    private final AuthenticationFacade authFacade;
    private final RssAtomParser feedParser;
    private final HeadScraper headScrapper;
    private final Collection<ScrappingHandler> scrappingHandlers;
    private final Map<String, FeedScrapperPlugin> plugins;

    public void startScrapping() {
        Instant now = clock.instant();
        Instant nextScrapping = now.plus(scrapFrequency);
        Duration toNextScrapping = Duration.between(now, nextScrapping);

        scheduleExecutor.scheduleAtFixedRate(this,
                toNextScrapping.getSeconds(), scrapFrequency.getSeconds(), TimeUnit.SECONDS);
        log.debug("Next scrapping at {}", LocalDateTime.now().plus(toNextScrapping));
        scheduleExecutor.schedule(this, 0, TimeUnit.MILLISECONDS);
    }

    @SneakyThrows
    public void shutdownScrapping() {
        if (!lock.tryAcquire(60, TimeUnit.SECONDS)) {
            log.warn("Unable to stop threads gracefully ! Threads was killed !");
        }
        scrapperScheduler.dispose();
        scheduleExecutor.shutdownNow();
        lock.release();
        log.info("All scrapper tasks finished and stopped !");
    }

    @Override
    @SneakyThrows
    public void run() {
        if (!lock.tryAcquire()) {
            log.warn("Scrapping in progress !");
        }
        log.info("Start scrapping ...");
        Mono<Set<String>> alreadyHave = newsRepository.list()
                .map(News::getId)
                .collect(Collectors.toUnmodifiableSet())
                .cache();

        Flux.concat(scrappingHandlers.stream().map(ScrappingHandler::before).toList())
                .thenMany(feedRepository.list())
                .parallel(4).runOn(scrapperScheduler)
                .flatMap(this::wgetFeedNews)
                .sequential()
                .filterWhen(n -> alreadyHave.map(l -> !l.contains(n.getId())))
                .parallel(4).runOn(scrapperScheduler)
                .flatMap(this::completeWithOpenGraph)
                .sequential()
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
                .contextWrite(authFacade.withSystemAuthentication())
                .subscribe();
    }

    @SuppressWarnings("java:S2095")
    private Flux<News> wgetFeedNews(Feed feed) {
        try {
            String feedHost = feed.getUrl().getHost();
            FeedScrapperPlugin hostPlugin = plugins.get(feedHost);
            URI feedUrl = (hostPlugin != null) ? hostPlugin.uriModifier(feed.getUrl()) : feed.getUrl();

            log.debug("Start scrapping feed {} ...", feedHost);
            PipedOutputStream osPipe = new PipedOutputStream();
            PipedInputStream isFeedPayload = new PipedInputStream(osPipe);

            Flux<DataBuffer> buffers = http.get()
                    .uri(feedUrl)
                    .accept(MediaType.APPLICATION_ATOM_XML)
                    .accept(MediaType.APPLICATION_RSS_XML)
                    .acceptCharset(StandardCharsets.UTF_8)
                    .retrieve()
                    .bodyToFlux(DataBuffer.class)
                    .doFirst(() -> log.trace("Receiving data from {}...", feedHost))
                    .onErrorResume(e -> {
                        log.warn(ERROR_CLASS_MESSAGE, feedHost, e.getLocalizedMessage());
                        log.debug(ERROR_STACKTRACE_MESSAGE, e);
                        return Flux.empty();
                    });

            Disposable feedReadingSubscription = DataBufferUtils.write(buffers, osPipe)
                    .onErrorContinue((e, buffer) -> {
                        log.warn(ERROR_CLASS_MESSAGE, feedHost, e.getLocalizedMessage());
                        log.debug(ERROR_STACKTRACE_MESSAGE, e);
                        DataBufferUtils.release((DataBuffer) buffer);
                    })
                    .doFinally(Exceptions.silence().consumer(Exceptions.wrap().consumer(signal -> {
                        osPipe.flush();
                        osPipe.close();
                        log.debug("Finish Scrapping feed {}.", feedHost);
                    }))).subscribe(DataBufferUtils.releaseConsumer());

            return feedParser.parse(feed, isFeedPayload)
                    .doOnComplete(feedReadingSubscription::dispose)
                    .doFinally(Exceptions.wrap().consumer(signal -> {
                        isFeedPayload.close();
                        log.trace("Finish Parsing feed {}.", feedHost);
                    }));
        } catch (IOException e) {
            log.error(ERROR_CLASS_MESSAGE, e.getClass(), e.getLocalizedMessage());
            log.debug(ERROR_STACKTRACE_MESSAGE, e);
        }
        return Flux.empty();
    }

    private Mono<News> completeWithOpenGraph(News news) {
        try {
            return headScrapper.scrap(news.getLink())
                    .map(headMetas -> {
                        RawNews raw = news.getRaw();

                        Links links = headMetas.links();
                        if (nonNull(links) && nonNull(links.canonical())) {
                            raw = raw.withLink(links.canonical());
                        }

                        OpenGraph og = headMetas.og();
                        if (og.isEmpty()) {
                            log.debug("No OG meta found for {}", news.getLink());
                            return (news.getRaw() == raw) ? news : news.withRaw(raw);
                        }
                        raw = Optional.ofNullable(og.title).map(raw::withTitle).orElse(raw);
                        raw = Optional.ofNullable(og.description).map(raw::withDescription).orElse(raw);
                        raw = Optional.ofNullable(og.image)
                                .filter(i -> SUPPORTED_SCHEMES.contains(i.getScheme()))
                                .map(raw::withImage).orElse(raw);
                        return (news.getRaw() == raw) ? news : news.withRaw(raw);
                    }).switchIfEmpty(Mono.just(news));
        } catch (Exception e) {
            log.warn("Unable to scrap header from {}.", news.getLink());
            log.debug(ERROR_STACKTRACE_MESSAGE, e);
            return Mono.just(news);
        }
    }

    @VisibleForTesting
    public boolean isScrapping() {
        return lock.availablePermits() == 0;
    }
}
