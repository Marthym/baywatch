package fr.ght1pc9kc.baywatch.infra.adapters;

import fr.ght1pc9kc.baywatch.api.model.Feed;
import fr.ght1pc9kc.baywatch.api.FeedPersistencePort;
import fr.ght1pc9kc.baywatch.dsl.tables.records.FeedsRecord;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jooq.Cursor;
import org.jooq.DSLContext;
import org.springframework.core.convert.ConversionService;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Scheduler;

import java.util.concurrent.atomic.AtomicInteger;

import static fr.ght1pc9kc.baywatch.dsl.tables.Feeds.FEEDS;

@Slf4j
@Repository
@AllArgsConstructor
public class FeedRepository implements FeedPersistencePort {
    private final Scheduler databaseScheduler;
    private final DSLContext dsl;
    private final ConversionService conversionService;

    @Override
    @SuppressWarnings("ReactiveStreamsNullableInLambdaInTransform")
    public Flux<Feed> list() {
        return Flux.<FeedsRecord>create(sink -> {
            AtomicInteger count = new AtomicInteger(0);
            Cursor<FeedsRecord> feedsCursor = dsl.selectFrom(FEEDS).fetchLazy();
            sink.onRequest(n -> feedsCursor.fetchNext(Long.valueOf(n).intValue())
                    .forEach(r -> {
                        sink.next(r);
                        count.incrementAndGet();
                    }));
            log.debug("Complete read for {} feed.", count.get());
            sink.complete();
        }).subscribeOn(databaseScheduler).map(fr -> conversionService.convert(fr, Feed.class));
    }
}
