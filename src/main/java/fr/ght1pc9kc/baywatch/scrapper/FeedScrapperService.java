package fr.ght1pc9kc.baywatch.scrapper;

import com.machinezoo.noexception.Exceptions;
import fr.ght1pc9kc.baywatch.dsl.tables.records.FeedsRecord;
import fr.ght1pc9kc.baywatch.dsl.tables.records.NewsRecord;
import fr.ght1pc9kc.baywatch.model.Feed;
import fr.ght1pc9kc.baywatch.model.News;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jooq.DSLContext;
import org.springframework.core.convert.ConversionService;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.MediaType;
import org.springframework.scheduling.concurrent.CustomizableThreadFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Scheduler;

import javax.annotation.PostConstruct;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static fr.ght1pc9kc.baywatch.dsl.tables.Feeds.FEEDS;

@Slf4j
@Service
@AllArgsConstructor
public final class FeedScrapperService implements Runnable {

    private final ThreadFactory threadFactory = new CustomizableThreadFactory("scrapper-");
    private final ScheduledExecutorService scheduleExecutor = Executors.newSingleThreadScheduledExecutor(
            new CustomizableThreadFactory("scrapSchec-"));
    private final Executor scrapperExecutor = Executors.newFixedThreadPool(
            4, new CustomizableThreadFactory("scrapper-"));
    private final WebClient http = WebClient.create();
    private final Clock clock;
    private final Scheduler databaseScheduler;
    private final DSLContext dsl;
    private final ConversionService conversionService;

    @PostConstruct
    private void startScrapping() {
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
        Executors.newSingleThreadExecutor(threadFactory).submit(this);
    }

    @Override
    public void run() {
        log.info("Start scrapping ...");
        readAllFeed()
                .flatMap(this::wgetFeedNews)
                .buffer(100)
                .publishOn(databaseScheduler)
                .subscribe(news -> {
                    List<NewsRecord> records = news.stream()
                            .map(n -> conversionService.convert(n, NewsRecord.class))
                            .collect(Collectors.toList());
                    dsl.batchInsert(records).execute();
                });
    }

    private Flux<Feed> readAllFeed() {
        return Flux.<FeedsRecord>create(sink -> {
            AtomicInteger count = new AtomicInteger(0);
            dsl.selectFrom(FEEDS).fetchLazy().forEach(r -> {
                sink.next(r);
                count.incrementAndGet();
            });
            log.debug("Complete read for {} feed.", count.get());
            sink.complete();
        }).subscribeOn(databaseScheduler)
                .map(fr -> conversionService.convert(fr, Feed.class));
    }

    private Flux<News> wgetFeedNews(Feed feed) {
        try {
            PipedOutputStream osPipe = new PipedOutputStream();
            PipedInputStream isPipe = new PipedInputStream(osPipe);
            XMLInputFactory xmlInputFactory = XMLInputFactory.newInstance();
            XMLEventReader reader = xmlInputFactory.createXMLEventReader(isPipe, StandardCharsets.UTF_8.displayName());

            Flux<DataBuffer> buffers = http.get()
                    .accept(MediaType.APPLICATION_ATOM_XML)
                    .accept(MediaType.APPLICATION_RSS_XML)
                    .acceptCharset(StandardCharsets.UTF_8)
                    .retrieve()
                    .bodyToFlux(DataBuffer.class);

            DataBufferUtils.write(buffers, osPipe)
                    .doOnComplete(Exceptions.wrap().runnable(() -> {
                        osPipe.flush();
                        osPipe.close();
                    })).subscribe(DataBufferUtils.releaseConsumer());

            return new FeedParser(reader).itemToFlux();
        } catch (IOException | XMLStreamException e) {
            log.error("{}: {}", e.getClass(), e.getLocalizedMessage());
            log.debug("STACKTRACE", e);
        }
        return Flux.empty();
    }
}
