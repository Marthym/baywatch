package fr.ght1pc9kc.baywatch.infra.adapters.persistence;

import fr.ght1pc9kc.baywatch.api.FeedPersistencePort;
import fr.ght1pc9kc.baywatch.api.model.Feed;
import fr.ght1pc9kc.baywatch.dsl.tables.records.FeedsRecord;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jooq.Cursor;
import org.jooq.DSLContext;
import org.jooq.Result;
import org.springframework.core.convert.ConversionService;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;

import java.util.Collection;

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
            Cursor<FeedsRecord> feedsCursor = dsl.selectFrom(FEEDS).fetchLazy();
            sink.onRequest(n -> {
                Result<FeedsRecord> rs = feedsCursor.fetchNext(Long.valueOf(n).intValue());
                rs.forEach(sink::next);
                if (rs.size() < n) {
                    sink.complete();
                }
            });
        })
                .limitRate(Integer.MAX_VALUE - 1) // Long.MAX_VALUE.intValue() == -1
                .subscribeOn(databaseScheduler).map(fr -> conversionService.convert(fr, Feed.class));
    }

    @Override
    public Mono<Void> persist(Collection<Feed> toCreate) {
        return null;
    }
}
