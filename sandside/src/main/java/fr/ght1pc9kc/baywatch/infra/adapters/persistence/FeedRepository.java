package fr.ght1pc9kc.baywatch.infra.adapters.persistence;

import fr.ght1pc9kc.baywatch.api.model.EntitiesProperties;
import fr.ght1pc9kc.baywatch.api.model.Feed;
import fr.ght1pc9kc.baywatch.domain.techwatch.ports.FeedPersistencePort;
import fr.ght1pc9kc.baywatch.dsl.tables.records.FeedsRecord;
import fr.ght1pc9kc.baywatch.dsl.tables.records.FeedsUsersRecord;
import fr.ght1pc9kc.baywatch.infra.mappers.BaywatchMapper;
import fr.ght1pc9kc.baywatch.infra.mappers.PropertiesMappers;
import fr.ght1pc9kc.baywatch.infra.request.filter.PredicateSearchVisitor;
import fr.ght1pc9kc.juery.api.Criteria;
import fr.ght1pc9kc.juery.api.PageRequest;
import fr.ght1pc9kc.juery.jooq.filter.JooqConditionVisitor;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jooq.*;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static fr.ght1pc9kc.baywatch.dsl.tables.Feeds.FEEDS;
import static fr.ght1pc9kc.baywatch.dsl.tables.FeedsUsers.FEEDS_USERS;

@Slf4j
@Repository
@AllArgsConstructor
public class FeedRepository implements FeedPersistencePort {
    private static final JooqConditionVisitor JOOQ_CONDITION_VISITOR =
            new JooqConditionVisitor(PropertiesMappers.FEEDS_PROPERTIES_MAPPING::get);
    private static final PredicateSearchVisitor<Feed> FEEDS_PREDICATE_VISITOR = new PredicateSearchVisitor<>();

    private final Scheduler databaseScheduler;
    private final DSLContext dsl;
    private final BaywatchMapper baywatchMapper;

    @Override
    public Mono<Feed> get(String id) {
        return list(PageRequest.one(Criteria.property(EntitiesProperties.ID).eq(id))).next();
    }

    @Override
    public Flux<Feed> list() {
        return list(PageRequest.all());
    }

    @Override
    public Flux<Feed> list(PageRequest pageRequest) {
        Condition conditions = pageRequest.filter().accept(JOOQ_CONDITION_VISITOR);
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
                .map(baywatchMapper::recordToFeed)
                .filter(pageRequest.filter().accept(FEEDS_PREDICATE_VISITOR));
    }

    @Override
    public Mono<Void> persist(Collection<Feed> toPersist) {

        List<FeedsRecord> records = toPersist.stream()
                .map(baywatchMapper::feedToFeedsRecord)
                .collect(Collectors.toList());

        return Mono.fromCallable(() ->
                dsl.loadInto(FEEDS)
                        .batchAll()
                        .onDuplicateKeyIgnore()
                        .onErrorIgnore()
                        .loadRecords(records)
                        .fieldsCorresponding()
                        .execute())
                .subscribeOn(databaseScheduler)

                .map(loader -> {
                    log.debug("Load {} Feeds with {} error(s) and {} ignored",
                            loader.processed(), loader.errors().size(), loader.ignored());
                    return loader;

                }).then();
    }

    @Override
    public Mono<Void> persist(Collection<Feed> toPersist, String userId) {
        List<FeedsUsersRecord> feedsUsersRecords = toPersist.stream()
                .map(baywatchMapper::feedToFeedsUsersRecord)
                .filter(Objects::nonNull)
                .map(r -> r.setFeusUserId(userId))
                .collect(Collectors.toList());

        return persist(toPersist)
                .then(Mono.fromCallable(() ->
                        dsl.loadInto(FEEDS_USERS)
                                .batchAll()
                                .onDuplicateKeyIgnore()
                                .onErrorIgnore()
                                .loadRecords(feedsUsersRecords)
                                .fieldsCorresponding()
                                .execute())
                        .subscribeOn(databaseScheduler))
                .then();
    }

    @Override
    public Mono<Integer> delete(Collection<String> toDelete) {
        return Mono.fromCallable(() ->
                dsl.deleteFrom(FEEDS_USERS).where(FEEDS_USERS.FEUS_FEED_ID.in(toDelete)).execute());
    }

    @Override
    public Mono<Integer> delete(Collection<String> toDelete, String userId) {
        return Mono.fromCallable(() ->
                dsl.deleteFrom(FEEDS_USERS).where(FEEDS_USERS.FEUS_FEED_ID.in(toDelete))
                        .and(FEEDS_USERS.FEUS_USER_ID.eq(userId)).execute());
    }

}
