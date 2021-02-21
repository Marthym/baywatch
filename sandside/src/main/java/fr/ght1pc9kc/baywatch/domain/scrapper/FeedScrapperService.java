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
import org.springframework.context.annotation.DependsOn;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.MediaType;
import org.springframework.scheduling.concurrent.CustomizableThreadFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.StopWatch;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Collection;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor()
@DependsOn({"flyway", "flywayInitializer"}) // Wait after Flyway migrations
public final class FeedScrapperService implements Runnable {

    private final ScheduledExecutorService scheduleExecutor = Executors.newSingleThreadScheduledExecutor(
            new CustomizableThreadFactory("scrapSched-"));
    private final Scheduler scrapperScheduler =
            Schedulers.newBoundedElastic(5, Integer.MAX_VALUE, "scrapper");
    private final WebClient http = WebClient.create();
    private final CountDownLatch lock = new CountDownLatch(1);
    private final Clock clock = Clock.systemUTC();

    private final OpenGraphScrapper ogScrapper;
    private final FeedPersistencePort feedRepository;
    private final NewsPersistencePort newsRepository;
    private final RssAtomParser feedParser;
    private final Collection<PreScrappingAction> preScrappingActions;

    @PostConstruct
    void startScrapping() {
        Instant now = clock.instant();
        Duration scrappingFrequency = Duration.ofDays(1);
        Instant nextScrapping = now.plus(scrappingFrequency)
                .truncatedTo(ChronoUnit.DAYS)
                .plus(Duration.ofHours(7));
        Duration toNextScrapping = Duration.between(now, nextScrapping);

        scheduleExecutor.scheduleAtFixedRate(this,
                toNextScrapping.getSeconds(), scrappingFrequency.getSeconds(), TimeUnit.DAYS);
        log.info("Next scrapping in {}h {}m {}s",
                toNextScrapping.toHoursPart(), toNextScrapping.toMinutesPart(), toNextScrapping.toSecondsPart());
        Schedulers.single().schedule(this);
    }

    @PreDestroy
    @SneakyThrows
    void shutdownScrapping() {
        scheduleExecutor.shutdown();
        if (!lock.await(60, TimeUnit.SECONDS)) {
            log.warn("Unable to stop threads gracefully ! Threads was killed !");
        }
        scrapperScheduler.dispose();
        log.info("All scrapper tasks finished and stopped !");
    }

    @Override
    public void run() {
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
                    lock.countDown();
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
                    RawNews raw = news.getRaw();
                    raw = Optional.ofNullable(og.title).map(raw::withTitle).orElse(raw);
                    raw = Optional.ofNullable(og.description).map(raw::withDescription).orElse(raw);
                    raw = Optional.ofNullable(og.image).map(raw::withImage).orElse(raw);
                    return news.withRaw(raw);
                }).switchIfEmpty(Mono.just(news));
    }
}
