package fr.ght1pc9kc.baywatch.infra.adapters.persistence;

import fr.ght1pc9kc.baywatch.api.AuthenticationFacade;
import fr.ght1pc9kc.baywatch.api.FeedPersistencePort;
import fr.ght1pc9kc.baywatch.api.model.Feed;
import fr.ght1pc9kc.baywatch.api.model.search.Criteria;
import fr.ght1pc9kc.baywatch.dsl.tables.records.FeedsRecord;
import fr.ght1pc9kc.baywatch.dsl.tables.records.FeedsUsersRecord;
import fr.ght1pc9kc.baywatch.infra.mappers.PropertiesMappers;
import fr.ght1pc9kc.baywatch.infra.search.JooqConditionVisitor;
import fr.ght1pc9kc.baywatch.infra.search.PredicateSearchVisitor;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jooq.*;
import org.springframework.core.convert.ConversionService;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;
import reactor.util.function.Tuples;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static fr.ght1pc9kc.baywatch.dsl.tables.Feeds.FEEDS;
import static fr.ght1pc9kc.baywatch.dsl.tables.FeedsUsers.FEEDS_USERS;
import static fr.ght1pc9kc.baywatch.dsl.tables.NewsFeeds.NEWS_FEEDS;

@Slf4j
@Repository
@AllArgsConstructor
public class FeedRepository implements FeedPersistencePort {
    private static final JooqConditionVisitor JOOQ_CONDITION_VISITOR =
            new JooqConditionVisitor(PropertiesMappers.FEEDS_PROPERTIES_MAPPING::get);
    private static final PredicateSearchVisitor<Feed> FEEDS_PREDICATE_VISITOR = new PredicateSearchVisitor<>();

    private final Scheduler databaseScheduler;
    private final DSLContext dsl;
    private final ConversionService conversionService;
    private final AuthenticationFacade authFacade;

    @Override
    public Mono<Feed> get(String id) {
        return list(Criteria.property(PropertiesMappers.ID).eq(id)).next();
    }

    @Override
    public Flux<Feed> list() {
        return list(Criteria.none());
    }

    @Override
    @SuppressWarnings("ReactiveStreamsNullableInLambdaInTransform")
    public Flux<Feed> list(Criteria criteria) {
        Condition conditions = criteria.visit(JOOQ_CONDITION_VISITOR);
        return Flux.<Record>create(sink -> {
            Cursor<Record> cursor = dsl.select(FEEDS.fields()).select(FEEDS_USERS.FEUS_TAGS)
                    .from(FEEDS)
                    .leftJoin(FEEDS_USERS).on(FEEDS_USERS.FEUS_FEED_ID.eq(FEEDS.FEED_ID))
                    .where(conditions).fetchLazy();
            sink.onRequest(n -> {
                Result<Record> rs = cursor.fetchNext(Long.valueOf(n).intValue());
                rs.forEach(sink::next);
                if (rs.size() < n) {
                    sink.complete();
                }
            });
        }).limitRate(Integer.MAX_VALUE - 1).subscribeOn(databaseScheduler)
                .map(fr -> conversionService.convert(fr, Feed.class))
                .filter(criteria.visit(FEEDS_PREDICATE_VISITOR));
    }

    @Override
    public Mono<Void> persist(Collection<Feed> toPersist) {
        return authFacade.getConnectedUser().map(user -> {
            List<FeedsRecord> records = toPersist.stream()
                    .map(n -> conversionService.convert(n, FeedsRecord.class))
                    .collect(Collectors.toList());

            List<FeedsUsersRecord> feedsUsersRecords = toPersist.stream()
                    .map(n -> conversionService.convert(n, FeedsUsersRecord.class))
                    .filter(Objects::nonNull)
                    .map(r -> r.setFeusUserId(user.id))
                    .collect(Collectors.toList());

            return Tuples.of(records, feedsUsersRecords);

        }).flatMap(records -> Mono.fromCallable(() -> Tuples.of(
                dsl.loadInto(FEEDS)
                        .batchAll()
                        .onDuplicateKeyIgnore()
                        .onErrorIgnore()
                        .loadRecords(records.getT1())
                        .fieldsCorresponding()
                        .execute(), records.getT2()))
                .subscribeOn(databaseScheduler)

        ).map(result -> {
            Loader<FeedsRecord> loader = result.getT1();
            log.debug("Load {} Feeds with {} error(s) and {} ignored",
                    loader.processed(), loader.errors().size(), loader.ignored());
            return result.getT2();

        }).flatMap(feedsUsersRecords -> Mono.fromCallable(() ->
                dsl.loadInto(FEEDS_USERS)
                        .batchAll()
                        .onDuplicateKeyIgnore()
                        .onErrorIgnore()
                        .loadRecords(feedsUsersRecords)
                        .fieldsCorresponding()
                        .execute())
                .subscribeOn(databaseScheduler)
        ).then();
    }

    @Override
    public Mono<Integer> delete(Collection<String> toDelete) {
        return Mono.fromCallable(() ->
                dsl.transactionResult(tx -> {
                    DSLContext txDsl = tx.dsl();
                    txDsl.deleteFrom(FEEDS_USERS).where(FEEDS_USERS.FEUS_FEED_ID.in(toDelete)).execute();
                    txDsl.deleteFrom(NEWS_FEEDS).where(NEWS_FEEDS.NEFE_FEED_ID.in(toDelete)).execute();
                    return txDsl.deleteFrom(FEEDS).where(FEEDS.FEED_ID.in(toDelete)).execute();
                }));
    }
}
