package fr.ght1pc9kc.baywatch.infra.adapters.persistence;

import fr.ght1pc9kc.baywatch.api.model.Feed;
import fr.ght1pc9kc.baywatch.domain.techwatch.model.QueryContext;
import fr.ght1pc9kc.baywatch.domain.techwatch.ports.FeedPersistencePort;
import fr.ght1pc9kc.baywatch.dsl.tables.records.FeedsRecord;
import fr.ght1pc9kc.baywatch.dsl.tables.records.FeedsUsersRecord;
import fr.ght1pc9kc.baywatch.infra.http.filter.PredicateSearchVisitor;
import fr.ght1pc9kc.baywatch.infra.mappers.BaywatchMapper;
import fr.ght1pc9kc.baywatch.infra.model.FeedDeletedResult;
import fr.ght1pc9kc.juery.jooq.filter.JooqConditionVisitor;
import fr.ght1pc9kc.juery.jooq.pagination.JooqPagination;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jooq.*;
import org.jooq.impl.DSL;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import static fr.ght1pc9kc.baywatch.dsl.tables.Feeds.FEEDS;
import static fr.ght1pc9kc.baywatch.dsl.tables.FeedsUsers.FEEDS_USERS;
import static fr.ght1pc9kc.baywatch.dsl.tables.NewsFeeds.NEWS_FEEDS;
import static fr.ght1pc9kc.baywatch.infra.mappers.PropertiesMappers.FEEDS_PROPERTIES_MAPPING;

@Slf4j
@Repository
@AllArgsConstructor
public class FeedRepository implements FeedPersistencePort {
    private static final JooqConditionVisitor JOOQ_CONDITION_VISITOR =
            new JooqConditionVisitor(FEEDS_PROPERTIES_MAPPING::get);
    private static final PredicateSearchVisitor<Feed> FEEDS_PREDICATE_VISITOR = new PredicateSearchVisitor<>();

    private final Scheduler databaseScheduler;
    private final DSLContext dsl;
    private final BaywatchMapper baywatchMapper;

    @Override
    public Mono<Feed> get(QueryContext qCtx) {
        return list(QueryContext.first(qCtx)).next();
    }

    @Override
    public Flux<Feed> list(QueryContext qCtx) {
        Select<Record> select = buildSelectQuery(qCtx);

        return Flux.<Record>create(sink -> {
                    Cursor<Record> cursor = select.fetchLazy();
                    sink.onRequest(n -> {
                        Result<Record> rs = cursor.fetchNext(Long.valueOf(n).intValue());
                        rs.forEach(sink::next);
                        if (rs.size() < n) {
                            sink.complete();
                        }
                    });
                }).limitRate(Integer.MAX_VALUE - 1).subscribeOn(databaseScheduler)
                .map(baywatchMapper::recordToFeed)
                .filter(qCtx.filter.accept(FEEDS_PREDICATE_VISITOR));
    }

    @Override
    public Mono<Integer> count(QueryContext qCtx) {
        Select<Record> select = buildSelectQuery(qCtx);
        return Mono.fromCallable(() -> dsl.fetchCount(select))
                .subscribeOn(databaseScheduler);
    }

    @Override
    public Mono<Feed> update(Feed toUpdate, String userId) {
        FeedsUsersRecord record = baywatchMapper.feedToFeedsUsersRecord(toUpdate);
        record.setFeusUserId(userId);
        return Mono.fromCallable(() -> dsl.executeUpdate(record))
                .subscribeOn(databaseScheduler)
                .flatMap((i) -> get(QueryContext.id(toUpdate.getId()).withUserId(userId)));
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
                                        .onDuplicateKeyUpdate()
                                        .onErrorIgnore()
                                        .loadRecords(feedsUsersRecords)
                                        .fieldsCorresponding()
                                        .execute())
                        .subscribeOn(databaseScheduler))
                .then();
    }

    @Override
    public Mono<FeedDeletedResult> delete(QueryContext qCtx) {
        Condition feedsUsersConditions = qCtx.filter.accept(FeedConditionsVisitors.feedUserIdVisitor());
        final Optional<Query> deleteUserLinkQuery;
        if (DSL.noCondition().equals(feedsUsersConditions)) {
            deleteUserLinkQuery = Optional.empty();
        } else {
            var query = dsl.deleteQuery(FEEDS_USERS);
            query.addConditions(feedsUsersConditions);
            if (qCtx.isScoped()) {
                query.addConditions(FEEDS_USERS.FEUS_USER_ID.eq(qCtx.userId));
            }
            deleteUserLinkQuery = Optional.of(query);
        }

        Condition newsFeedConditions = qCtx.filter.accept(FeedConditionsVisitors.newsFeedIdVisitor());
        final Optional<Query> deleteNewsFeedQuery;
        if (DSL.noCondition().equals(newsFeedConditions)) {
            deleteNewsFeedQuery = Optional.empty();
        } else {
            deleteNewsFeedQuery = Optional.of(dsl.deleteFrom(NEWS_FEEDS).where(newsFeedConditions)
                    .and(NEWS_FEEDS.NEFE_FEED_ID.notIn(
                            dsl.select(FEEDS_USERS.FEUS_FEED_ID).from(FEEDS_USERS).where(feedsUsersConditions))));
        }

        Condition feedsConditions = qCtx.filter.accept(FeedConditionsVisitors.feedIdVisitor());
        final Optional<Query> deleteFeedQuery;
        if (DSL.noCondition().equals(feedsConditions)) {
            deleteFeedQuery = Optional.empty();
        } else {

            deleteFeedQuery = Optional.of(dsl.deleteFrom(FEEDS).where(feedsConditions)
                    .and(FEEDS.FEED_ID.notIn(
                            dsl.select(FEEDS_USERS.FEUS_FEED_ID).from(FEEDS_USERS).where(feedsUsersConditions))));
        }

        return Mono.fromCallable(() ->
                dsl.transactionResult(tx -> {
                    int unsubscribed = deleteUserLinkQuery.map(q -> tx.dsl().execute(q)).orElse(0);
                    deleteNewsFeedQuery.map(q -> tx.dsl().execute(q));
                    int purged = deleteFeedQuery.map(q -> tx.dsl().execute(q)).orElse(0);
                    return new FeedDeletedResult(unsubscribed, purged);
                }));
    }

    private Select<Record> buildSelectQuery(QueryContext qCtx) {
        Condition conditions = qCtx.filter.accept(JOOQ_CONDITION_VISITOR);
        SelectQuery<Record> select = dsl.selectQuery();
        select.addSelect(FEEDS.fields());
        select.addFrom(FEEDS);
        select.addConditions(conditions);

        if (qCtx.isScoped()) {
            select.addSelect(FEEDS_USERS.FEUS_TAGS, FEEDS_USERS.FEUS_FEED_NAME);
            select.addJoin(FEEDS_USERS, JoinType.JOIN,
                    FEEDS.FEED_ID.eq(FEEDS_USERS.FEUS_FEED_ID).and(FEEDS_USERS.FEUS_USER_ID.eq(qCtx.userId)));
        } else {
            select.addSelect(DSL.count(FEEDS_USERS.FEUS_USER_ID).as(FEEDS_USERS.FEUS_USER_ID));
            select.addJoin(FEEDS_USERS, JoinType.LEFT_OUTER_JOIN,
                    FEEDS.FEED_ID.eq(FEEDS_USERS.FEUS_FEED_ID));
            select.addGroupBy(FEEDS.fields());
            Condition havings = qCtx.filter.accept(FeedConditionsVisitors.feedUserHavingVisitor());
            select.addHaving(havings);
        }

        return JooqPagination.apply(qCtx.pagination, FEEDS_PROPERTIES_MAPPING, select);
    }
}
