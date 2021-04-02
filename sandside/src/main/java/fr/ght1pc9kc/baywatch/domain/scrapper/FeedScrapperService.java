package fr.ght1pc9kc.baywatch.domain.scrapper;

import com.machinezoo.noexception.Exceptions;
import fr.ght1pc9kc.baywatch.api.model.Feed;
import fr.ght1pc9kc.baywatch.api.model.News;
import fr.ght1pc9kc.baywatch.api.model.RawNews;
import fr.ght1pc9kc.baywatch.api.scrapper.PreScrappingAction;
import fr.ght1pc9kc.baywatch.api.scrapper.RssAtomParser;
import fr.ght1pc9kc.baywatch.domain.ports.FeedPersistencePort;
import fr.ght1pc9kc.baywatch.domain.ports.NewsPersistencePort;
import fr.ght1pc9kc.baywatch.domain.scrapper.opengraph.OpenGraphScrapper;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.VisibleForTesting;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.MediaType;
import org.springframework.scheduling.concurrent.CustomizableThreadFactory;
import org.springframework.util.StopWatch;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
public final class FeedScrapperService implements Runnable {

    private static final Set<String> SUPPORTED_SCHEMES = Set.of("http", "https");
    private final ScheduledExecutorService scheduleExecutor = Executors.newSingleThreadScheduledExecutor(
            new CustomizableThreadFactory("scrapSched-"));
    private final Scheduler scrapperScheduler =
            Schedulers.newBoundedElastic(5, Integer.MAX_VALUE, "scrapper");
    private final WebClient http = WebClient.create();
    private final Clock clock = Clock.systemUTC();
    private final Semaphore lock = new Semaphore(1);

    private final Duration scrapFrequency;
    private final OpenGraphScrapper ogScrapper;
    private final FeedPersistencePort feedRepository;
    private final NewsPersistencePort newsRepository;
    private final RssAtomParser feedParser;
    private final Collection<PreScrappingAction> preScrappingActions;

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
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        log.info("Start scrapping ...");
        Mono<Set<String>> alreadyHave = newsRepository.list()
                .map(RawNews::getId)
                .collect(Collectors.toUnmodifiableSet())
                .cache();

        Flux.concat(preScrappingActions.stream().map(PreScrappingAction::call).collect(Collectors.toList()))
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
                .doOnError(e -> {
                    log.error("{}: {}", e.getClass(), e.getLocalizedMessage());
                    log.debug("STACKTRACE", e);
                })
                .doFinally(signal -> {
                    lock.release();
                    stopWatch.stop();
                    log.info("Scrapping finished with {} in {}", signal, Duration.ofMillis(stopWatch.getTotalTimeMillis()));
                })
                .subscribe();
    }

    private Flux<News> wgetFeedNews(Feed feed) {
        try {
            log.debug("Start scrapping feed {} ...", feed.getUrl().getHost());
            PipedOutputStream osPipe = new PipedOutputStream();
            PipedInputStream isFeedPayload = new PipedInputStream(osPipe);

            Flux<DataBuffer> buffers = http.get()
                    .uri(feed.getUrl())
                    .accept(MediaType.APPLICATION_ATOM_XML)
                    .accept(MediaType.APPLICATION_RSS_XML)
                    .acceptCharset(StandardCharsets.UTF_8)
                    .retrieve()
                    .bodyToFlux(DataBuffer.class)
                    .doFirst(() -> log.trace("Receiving data from {}...", feed.getUrl().getHost()))
                    .onErrorResume(e -> {
                        log.error("{}", e.getLocalizedMessage());
                        log.debug("STACKTRACE", e);
                        return Flux.empty();
                    });

            DataBufferUtils.write(buffers, osPipe)
                    .doFinally(Exceptions.wrap().consumer(signal -> {
                        osPipe.flush();
                        osPipe.close();
                        log.debug("Finish Scrapping feed {}.", feed.getUrl().getHost());
                    })).subscribe(DataBufferUtils.releaseConsumer());

            return feedParser.parse(feed, isFeedPayload)
                    .doFinally(Exceptions.wrap().consumer(signal -> {
                        isFeedPayload.close();
                        log.trace("Finish Parsing feed {}.", feed.getUrl().getHost());
                    }));
        } catch (IOException e) {
            log.error("{}: {}", e.getClass(), e.getLocalizedMessage());
            log.debug("STACKTRACE", e);
        }
        return Flux.empty();
    }

    private Mono<News> completeWithOpenGraph(News news) {
        return ogScrapper.scrap(news.getLink())
                .map(og -> {
                    if (og.isEmpty()) {
                        log.debug("No OG meta found for {}", news.getLink());
                        return news;
                    }
                    RawNews raw = news.getRaw();
                    raw = Optional.ofNullable(og.title).map(raw::withTitle).orElse(raw);
                    raw = Optional.ofNullable(og.description).map(raw::withDescription).orElse(raw);
                    raw = Optional.ofNullable(og.image)
                            .filter(i -> SUPPORTED_SCHEMES.contains(i.getScheme()))
                            .map(raw::withImage).orElse(raw);
                    return news.withRaw(raw);
                }).switchIfEmpty(Mono.just(news));
    }

    @VisibleForTesting
    public boolean isScrapping() {
        return lock.availablePermits() == 0;
    }
}
