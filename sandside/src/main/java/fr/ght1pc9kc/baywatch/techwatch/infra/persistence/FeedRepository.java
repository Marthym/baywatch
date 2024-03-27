package fr.ght1pc9kc.baywatch.techwatch.infra.persistence;

import fr.ght1pc9kc.baywatch.common.infra.DatabaseQualifier;
import fr.ght1pc9kc.baywatch.common.infra.adapters.PerformanceJooqListener;
import fr.ght1pc9kc.baywatch.common.infra.mappers.BaywatchMapper;
import fr.ght1pc9kc.baywatch.dsl.tables.records.FeedsRecord;
import fr.ght1pc9kc.baywatch.dsl.tables.records.FeedsUsersRecord;
import fr.ght1pc9kc.baywatch.techwatch.api.model.WebFeed;
import fr.ght1pc9kc.baywatch.common.domain.QueryContext;
import fr.ght1pc9kc.baywatch.techwatch.domain.ports.FeedPersistencePort;
import fr.ght1pc9kc.baywatch.techwatch.infra.model.FeedDeletedResult;
import fr.ght1pc9kc.entity.api.Entity;
import fr.ght1pc9kc.juery.api.Criteria;
import fr.ght1pc9kc.juery.jooq.filter.JooqConditionVisitor;
import fr.ght1pc9kc.juery.jooq.pagination.JooqPagination;
import lombok.extern.slf4j.Slf4j;
import org.jooq.Condition;
import org.jooq.Cursor;
import org.jooq.DSLContext;
import org.jooq.JoinType;
import org.jooq.Query;
import org.jooq.Record;
import org.jooq.Result;
import org.jooq.Select;
import org.jooq.SelectQuery;
import org.jooq.impl.DSL;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static fr.ght1pc9kc.baywatch.common.api.model.EntitiesProperties.ID;
import static fr.ght1pc9kc.baywatch.common.api.model.FeedMeta.createdBy;
import static fr.ght1pc9kc.baywatch.common.infra.mappers.PropertiesMappers.FEEDS_PROPERTIES_MAPPING;
import static fr.ght1pc9kc.baywatch.dsl.tables.Feeds.FEEDS;
import static fr.ght1pc9kc.baywatch.dsl.tables.FeedsUsers.FEEDS_USERS;
import static fr.ght1pc9kc.baywatch.dsl.tables.NewsFeeds.NEWS_FEEDS;

@Slf4j
@Repository
@SuppressWarnings("BlockingMethodInNonBlockingContext")
public class FeedRepository implements FeedPersistencePort {
    private static final JooqConditionVisitor JOOQ_CONDITION_VISITOR =
            new JooqConditionVisitor(FEEDS_PROPERTIES_MAPPING::get);

    private final Scheduler databaseScheduler;
    private final DSLContext dsl;
    private final BaywatchMapper baywatchMapper;

    public FeedRepository(@DatabaseQualifier Scheduler databaseScheduler, DSLContext dsl, BaywatchMapper baywatchMapper) {
        this.databaseScheduler = databaseScheduler;
        this.dsl = DSL.using(dsl.configuration().deriveAppending(PerformanceJooqListener.provider()));
        this.baywatchMapper = baywatchMapper;
    }

    @Override
    public Mono<Entity<WebFeed>> get(QueryContext qCtx) {
        return list(QueryContext.first(qCtx)).next();
    }

    @Override
    @SuppressWarnings("resource")
    public Flux<Entity<WebFeed>> list(QueryContext qCtx) {
        Select<Record> select = buildSelectQuery(qCtx);

        return Flux.<Record>create(sink -> {
                    Cursor<Record> cursor = select.fetchLazy();
                    sink.onRequest(n -> {
                        Result<Record> rs = cursor.fetchNext((int) n);
                        rs.forEach(sink::next);
                        if (rs.size() < n) {
                            sink.complete();
                        }
                    });
                }).limitRate(Integer.MAX_VALUE - 1)
                .subscribeOn(databaseScheduler)
                .map(baywatchMapper::recordToFeed);
    }

    @Override
    public Mono<Integer> count(QueryContext qCtx) {
        Select<Record> select = buildSelectQuery(qCtx);
        return Mono.fromCallable(() -> dsl.fetchCount(select))
                .subscribeOn(databaseScheduler);
    }

    @Override
    public Mono<Entity<WebFeed>> update(String id, WebFeed toUpdate) {
        return Mono.fromCallable(() -> dsl.update(FEEDS)
                        .set(FEEDS.FEED_NAME, toUpdate.name())
                        .set(FEEDS.FEED_DESCRIPTION, toUpdate.description())
                        .set(FEEDS.FEED_NAME, toUpdate.name())
                        .where(FEEDS.FEED_ID.eq(id))
                        .returning())
                .subscribeOn(databaseScheduler)
                .flatMap(result -> {
                    FeedsRecord feedsRecord = result.fetchOne();
                    if (feedsRecord != null) {
                        return Mono.just(baywatchMapper.recordToFeed(feedsRecord));
                    } else {
                        return get(QueryContext.id(id));
                    }
                });
    }

    @Override
    public Flux<Entity<WebFeed>> update(Collection<Entity<WebFeed>> toUpdate) {
        List<FeedsRecord> records = toUpdate.stream()
                .map(baywatchMapper::feedToFeedsRecord).toList();

        return Mono.fromCallable(() ->
                        dsl.loadInto(FEEDS)
                                .batchAll()
                                .onDuplicateKeyUpdate()
                                .onErrorIgnore()
                                .loadRecords(records)
                                .fieldsCorresponding()
                                .execute())
                .subscribeOn(databaseScheduler)

                .map(loader -> {
                    log.debug("Update {} Feeds with {} error(s) and {} ignored",
                            loader.stored(), loader.errors().size(), loader.ignored());
                    return loader;
                })

                .thenMany(Flux.fromIterable(toUpdate))
                .map(Entity::id)
                .flatMap(refs -> this.list(QueryContext.all(Criteria.property(ID).in(refs))));
    }

    @Override
    public Mono<Entity<WebFeed>> update(String id, String userId, WebFeed toUpdate) {
        Entity<WebFeed> webFeedEntity = Entity.identify(toUpdate)
                .meta(createdBy, userId)
                .withId(id);
        FeedsUsersRecord feedsUsersRecord = baywatchMapper.feedToFeedsUsersRecord(webFeedEntity);
        return Mono.fromCallable(() -> dsl.executeUpdate(feedsUsersRecord))
                .subscribeOn(databaseScheduler)
                .flatMap(i -> get(QueryContext.id(id).withUserId(userId)));
    }

    @Override
    public Flux<Entity<WebFeed>> persist(Collection<Entity<WebFeed>> toPersist) {
        List<FeedsRecord> records = toPersist.stream()
                .map(baywatchMapper::feedToFeedsRecord)
                .toList();

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
                            loader.stored(), loader.errors().size(), loader.ignored());
                    return loader;
                })

                .thenMany(Flux.fromIterable(toPersist))
                .map(Entity::id)
                .flatMap(refs -> this.list(QueryContext.all(Criteria.property(ID).in(refs))));
    }

    @Override
    public Flux<Entity<WebFeed>> persistUserRelation(Collection<Entity<WebFeed>> feeds, String userId) {
        List<FeedsUsersRecord> feedsUsersRecords = feeds.stream()
                .map(baywatchMapper::feedToFeedsUsersRecord)
                .filter(Objects::nonNull)
                .map(r -> r.setFeusUserId(userId))
                .toList();

        return Mono.fromCallable(() ->
                        dsl.loadInto(FEEDS_USERS)
                                .batchAll()
                                .onDuplicateKeyUpdate()
                                .onErrorIgnore()
                                .loadRecords(feedsUsersRecords)
                                .fieldsCorresponding()
                                .execute())
                .subscribeOn(databaseScheduler)
                .thenMany(Flux.fromIterable(feeds))
                .map(Entity::id)
                .flatMap(refs -> this.list(QueryContext.builder()
                        .userId(userId)
                        .filter(Criteria.property(ID).in(refs))
                        .build()));
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
                    deleteNewsFeedQuery.ifPresent(q -> tx.dsl().execute(q));
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
            select.addSelect(DSL.groupConcat(FEEDS_USERS.FEUS_USER_ID).as(FEEDS_USERS.FEUS_USER_ID));
            select.addJoin(FEEDS_USERS, JoinType.LEFT_OUTER_JOIN,
                    FEEDS.FEED_ID.eq(FEEDS_USERS.FEUS_FEED_ID));
            select.addGroupBy(FEEDS.fields());
            Condition havings = qCtx.filter.accept(FeedConditionsVisitors.feedUserHavingVisitor());
            select.addHaving(havings);
        }

        return JooqPagination.apply(qCtx.pagination, FEEDS_PROPERTIES_MAPPING, select);
    }
}
