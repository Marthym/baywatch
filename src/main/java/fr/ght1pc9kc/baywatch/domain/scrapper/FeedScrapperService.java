package fr.ght1pc9kc.baywatch.domain.scrapper;

import com.machinezoo.noexception.Exceptions;
import fr.ght1pc9kc.baywatch.api.FeedPersistencePort;
import fr.ght1pc9kc.baywatch.api.NewsPersistencePort;
import fr.ght1pc9kc.baywatch.api.RssAtomParser;
import fr.ght1pc9kc.baywatch.api.model.Feed;
import fr.ght1pc9kc.baywatch.api.model.News;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.DependsOn;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.MediaType;
import org.springframework.scheduling.concurrent.CustomizableThreadFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
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
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
@DependsOn({"flyway", "flywayInitializer"}) // Wait after Flyway migrations
public final class FeedScrapperService implements Runnable {

    private final ScheduledExecutorService scheduleExecutor = Executors.newSingleThreadScheduledExecutor(
            new CustomizableThreadFactory("scrapSched-"));
    private final Scheduler scrapperScheduler =
            Schedulers.newBoundedElastic(4, Integer.MAX_VALUE, "scrapper");
    private final WebClient http = WebClient.create();
    private final Clock clock;
    private final FeedPersistencePort feedRepository;
    private final NewsPersistencePort newsRepository;
    private final RssAtomParser feedParser;
    private final CountDownLatch lock = new CountDownLatch(1);

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
        lock.await(60, TimeUnit.SECONDS);
        scrapperScheduler.dispose();
        log.info("All scrapper tasks finished and stopped !");
    }

    @Override
    public void run() {
        log.info("Start scrapping ...");
        feedRepository.list()
                .parallel(4).runOn(scrapperScheduler)
                .flatMap(this::wgetFeedNews)
                .sequential()
                .buffer(100)
                .flatMap(newsRepository::create)
                .doOnError(e -> {
                    log.error("{}: {}", e.getClass(), e.getLocalizedMessage());
                    log.debug("STACKTRACE", e);
                })
                .doFinally(signal -> lock.countDown())
                .subscribe();
    }

    private Flux<News> wgetFeedNews(Feed feed) {
        try {
            log.debug("Start scrapping feed {} ...", feed.getUrl().getHost());
            PipedOutputStream osPipe = new PipedOutputStream();
            PipedInputStream isPipe = new PipedInputStream(osPipe);

            Flux<DataBuffer> buffers = http.get()
                    .uri(feed.getUrl())
                    .accept(MediaType.APPLICATION_ATOM_XML)
                    .accept(MediaType.APPLICATION_RSS_XML)
                    .acceptCharset(StandardCharsets.UTF_8)
                    .retrieve()
                    .bodyToFlux(DataBuffer.class)
                    .doFirst(() -> log.trace("Receiving data from {}...", feed.url.getHost()))
                    //FIXME: Faudrait surement faire remonter à l'appelant pour faire quelque chose de l'erreur et pour ne pas parser
                    .onErrorResume(e -> {
                        log.error("{}", e.getLocalizedMessage());
                        log.debug("STACKTRACE", e);
                        return Flux.empty();
                    });

            DataBufferUtils.write(buffers, osPipe)
                    .doFinally(Exceptions.wrap().consumer(signal -> {
                        isPipe.close();
                        osPipe.flush();
                        osPipe.close();
                        log.debug("Finish Scrapping feed {}.", feed.getUrl().getHost());
                    })).subscribe(DataBufferUtils.releaseConsumer());

            return feedParser.parse(isPipe);
        } catch (IOException e) {
            log.error("{}: {}", e.getClass(), e.getLocalizedMessage());
            log.debug("STACKTRACE", e);
        }
        return Flux.empty();
    }
}
