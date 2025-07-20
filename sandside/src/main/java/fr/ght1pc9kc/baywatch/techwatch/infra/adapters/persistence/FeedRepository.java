package fr.ght1pc9kc.baywatch.techwatch.infra.adapters.persistence;

import fr.ght1pc9kc.baywatch.common.domain.QueryContext;
import fr.ght1pc9kc.baywatch.common.infra.DatabaseQualifier;
import fr.ght1pc9kc.baywatch.common.infra.adapters.PerformanceJooqListener;
import fr.ght1pc9kc.baywatch.dsl.tables.records.FeedsRecord;
import fr.ght1pc9kc.baywatch.dsl.tables.records.FeedsUsersPropertiesRecord;
import fr.ght1pc9kc.baywatch.dsl.tables.records.FeedsUsersRecord;
import fr.ght1pc9kc.baywatch.techwatch.api.model.WebFeed;
import fr.ght1pc9kc.baywatch.techwatch.domain.ports.FeedPersistencePort;
import fr.ght1pc9kc.baywatch.techwatch.infra.config.TechwatchMapper;
import fr.ght1pc9kc.baywatch.techwatch.infra.model.FeedDeletedResult;
import fr.ght1pc9kc.baywatch.techwatch.infra.model.FeedProperties;
import fr.ght1pc9kc.entity.api.Entity;
import fr.ght1pc9kc.juery.api.Criteria;
import fr.ght1pc9kc.juery.basic.filter.ListPropertiesCriteriaVisitor;
import fr.ght1pc9kc.juery.jooq.filter.JooqConditionVisitor;
import fr.ght1pc9kc.juery.jooq.pagination.JooqPagination;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.Nullable;
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
import reactor.util.function.Tuples;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static fr.ght1pc9kc.baywatch.common.api.model.EntitiesProperties.ID;
import static fr.ght1pc9kc.baywatch.common.api.model.EntitiesProperties.TAGS;
import static fr.ght1pc9kc.baywatch.common.api.model.FeedMeta.createdBy;
import static fr.ght1pc9kc.baywatch.common.infra.mappers.PropertiesMappers.FEEDS_PROPERTIES_MAPPING;
import static fr.ght1pc9kc.baywatch.dsl.tables.Feeds.FEEDS;
import static fr.ght1pc9kc.baywatch.dsl.tables.FeedsErrors.FEEDS_ERRORS;
import static fr.ght1pc9kc.baywatch.dsl.tables.FeedsUsers.FEEDS_USERS;
import static fr.ght1pc9kc.baywatch.dsl.tables.FeedsUsersProperties.FEEDS_USERS_PROPERTIES;
import static fr.ght1pc9kc.baywatch.techwatch.infra.adapters.persistence.FeedConditionsVisitors.FEED_ERRORS_VISITOR;
import static fr.ght1pc9kc.baywatch.techwatch.infra.adapters.persistence.FeedConditionsVisitors.FEED_USERS_PROPERTIES_VISITOR;
import static java.util.Objects.nonNull;
import static java.util.Objects.requireNonNull;

@Slf4j
@Repository
@SuppressWarnings("BlockingMethodInNonBlockingContext")
public class FeedRepository implements FeedPersistencePort {
    private static final JooqConditionVisitor JOOQ_CONDITION_VISITOR =
            new JooqConditionVisitor(FEEDS_PROPERTIES_MAPPING::get);
    private static final ListPropertiesCriteriaVisitor PROPERTIES_CRITERIA_VISITOR =
            new ListPropertiesCriteriaVisitor();

    private final Scheduler databaseScheduler;
    private final DSLContext dsl;
    private final TechwatchMapper mapper;

    public FeedRepository(@DatabaseQualifier Scheduler databaseScheduler, DSLContext dsl, TechwatchMapper mapper) {
        this.databaseScheduler = databaseScheduler;
        this.dsl = DSL.using(dsl.configuration().deriveAppending(PerformanceJooqListener.provider()));
        this.mapper = mapper;
    }

    @Override
    public Mono<Entity<WebFeed>> get(QueryContext qCtx) {
        return list(QueryContext.first(qCtx)).next();
    }

    @Override
    public Flux<Entity<Map<FeedProperties, String>>> getFeedProperties(
            String userId, Collection<String> feedIds, @Nullable EnumSet<FeedProperties> properties) {
        if (feedIds.isEmpty()) {
            return Flux.error(() -> new IllegalArgumentException("Feed IDs must not be empty"));
        }

        SelectQuery<FeedsUsersPropertiesRecord> query = dsl.selectQuery(FEEDS_USERS_PROPERTIES);
        query.addConditions(FEEDS_USERS_PROPERTIES.FUPR_USER_ID.eq(userId));
        if (feedIds.size() > 1) {
            query.addConditions(FEEDS_USERS_PROPERTIES.FUPR_FEED_ID.in(feedIds));
        } else {
            query.addConditions(FEEDS_USERS_PROPERTIES.FUPR_FEED_ID.eq(feedIds.iterator().next()));
        }
        if (nonNull(properties)) {
            if (properties.size() == 1) {
                query.addConditions(FEEDS_USERS_PROPERTIES.FUPR_PROPERTY_NAME.eq(properties.iterator().next().name()));
            } else if (!properties.isEmpty()) {
                query.addConditions(FEEDS_USERS_PROPERTIES.FUPR_PROPERTY_NAME.in(properties.stream().map(FeedProperties::name).toList()));
            }
        }
        query.addOrderBy(FEEDS_USERS_PROPERTIES.FUPR_USER_ID, FEEDS_USERS_PROPERTIES.FUPR_FEED_ID);

        return Flux.<FeedsUsersPropertiesRecord>create(sink -> {
                    //noinspection resource
                    Cursor<FeedsUsersPropertiesRecord> cursor = query.fetchLazy();
                    sink.onRequest(n -> {
                        Result<FeedsUsersPropertiesRecord> rs = cursor.fetchNext((int) n);
                        rs.forEach(sink::next);
                        if (rs.size() < n) {
                            sink.complete();
                            cursor.close();
                        }
                    });
                })
                .limitRate(Integer.MAX_VALUE - 1)
                .subscribeOn(databaseScheduler)
                .bufferUntilChanged(r -> Tuples.of(r.getFuprUserId(), r.getFuprFeedId()))
                .map(props -> {
                    FeedsUsersPropertiesRecord first = props.getFirst();
                    Map<FeedProperties, String> mapProperties = props.stream().map(prop -> Map.entry(FeedProperties.valueOf(prop.getFuprPropertyName()), prop.getFuprPropertyValue()))
                            .collect(Collectors.toUnmodifiableMap(Map.Entry::getKey, Map.Entry::getValue, (left, right) -> left + "," + right));
                    return Entity.identify(mapProperties)
                            .meta(createdBy, first.getFuprUserId())
                            .withId(first.getFuprFeedId());
                });
    }

    public Mono<Void> setFeedProperties(String userId, Collection<Entity<WebFeed>> feeds) {
        var records = new ArrayList<FeedsUsersPropertiesRecord>(feeds.size());
        List<String> feedsIds = feeds.stream().map(Entity::id).distinct().toList();
        for (Entity<WebFeed> feed : feeds) {
            if (nonNull(feed.self())) {
                if (nonNull(feed.self().name()) && !feed.self().name().isEmpty()) {
                    records.add(FEEDS_USERS_PROPERTIES.newRecord()
                            .setFuprFeedId(feed.id())
                            .setFuprUserId(userId)
                            .setFuprPropertyName(FeedProperties.NAME.name())
                            .setFuprPropertyValue(feed.self().name()));
                }
                if (nonNull(feed.self().description()) && !feed.self().description().isEmpty()) {
                    records.add(FEEDS_USERS_PROPERTIES.newRecord()
                            .setFuprFeedId(feed.id())
                            .setFuprUserId(userId)
                            .setFuprPropertyName(FeedProperties.DESCRIPTION.name())
                            .setFuprPropertyValue(feed.self().description()));
                }
                if (!feed.self().tags().isEmpty()) {
                    feed.self().tags().forEach(tag -> records.add(FEEDS_USERS_PROPERTIES.newRecord()
                            .setFuprFeedId(feed.id())
                            .setFuprUserId(userId)
                            .setFuprPropertyName(FeedProperties.TAG.name())
                            .setFuprPropertyValue(tag)));
                }
            }
        }

        return Mono.fromCallable(() -> dsl.transactionResult(tx -> {
                    tx.dsl().deleteFrom(FEEDS_USERS_PROPERTIES)
                            .where(FEEDS_USERS_PROPERTIES.FUPR_USER_ID.eq(userId)
                                    .and(FEEDS_USERS_PROPERTIES.FUPR_FEED_ID.in(feedsIds)))
                            .execute();
                    return tx.dsl().batchInsert(records).execute();
                })).subscribeOn(databaseScheduler)
                .then();
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
                .map(mapper::recordToFeed);
    }

    @Override
    public Mono<Integer> count(QueryContext qCtx) {
        Select<Record> select = buildSelectQuery(qCtx);
        return Mono.fromCallable(() -> dsl.fetchCount(select))
                .subscribeOn(databaseScheduler);
    }

    @Override
    public Flux<Entity<WebFeed>> update(Collection<Entity<WebFeed>> toUpdate) {
        List<FeedsRecord> records = toUpdate.stream()
                .map(mapper::feedToFeedsRecord).toList();

        return Mono.fromCallable(() ->
                        dsl.batchUpdate(records).execute())
                .subscribeOn(databaseScheduler)

                .map(batch -> {
                    int updated = Arrays.stream(batch).sum();
                    log.debug("Update {} Feeds successfully.", updated);
                    return batch;
                })

                .thenMany(Flux.fromIterable(toUpdate))
                .map(Entity::id)
                .flatMap(refs -> this.list(QueryContext.all(Criteria.property(ID).in(refs))));
    }

    @Override
    public Flux<Entity<WebFeed>> persist(Collection<Entity<WebFeed>> toPersist) {
        List<FeedsRecord> records = toPersist.stream()
                .map(mapper::feedToFeedsRecord)
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
    public Flux<Entity<WebFeed>> persistUserRelation(String userId, Collection<Entity<WebFeed>> feeds) {
        List<FeedsUsersRecord> feedsUsersRecords = feeds.stream()
                .map(f -> FEEDS_USERS.newRecord()
                        .setFeusFeedId(f.id())
                        .setFeusUserId(userId))
                .toList();

        return Mono.fromCallable(() ->
                        dsl.loadInto(FEEDS_USERS)
                                .batchAll()
                                .onDuplicateKeyIgnore()
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

    /**
     * @param qCtx Context of the query, containing the filter.
     * @return Result of deletion
     */
    @Override
    public Mono<FeedDeletedResult> delete(QueryContext qCtx) {
        List<Condition> feedsUsersConditions = new ArrayList<>(2);
        feedsUsersConditions.add(qCtx.filter().accept(FeedConditionsVisitors.feedUserIdVisitor()));
        if (qCtx.isScoped()) {
            feedsUsersConditions.add(FEEDS_USERS.FEUS_USER_ID.eq(qCtx.userId()));
        }
        if (DSL.noCondition().equals(DSL.and(feedsUsersConditions))) {
            return Mono.error(() -> new IllegalArgumentException("No feed user condition"));
        }
        var deleteUserLinkQuery = dsl.deleteQuery(FEEDS_USERS);
        deleteUserLinkQuery.addConditions(feedsUsersConditions);

        Condition feedErrorsConditions = qCtx.filter().accept(FEED_ERRORS_VISITOR);
        if (DSL.noCondition().equals(feedErrorsConditions)) {
            return Mono.error(() -> new IllegalArgumentException("No feed errors condition"));
        }
        var deleteFeedsErrorsQuery = dsl.deleteQuery(FEEDS_ERRORS);
        deleteFeedsErrorsQuery.addConditions(feedErrorsConditions);

        List<Condition> feedUsersPropertiesConditions = new ArrayList<>(2);
        feedUsersPropertiesConditions.add(qCtx.filter().accept(FEED_USERS_PROPERTIES_VISITOR));
        if (qCtx.isScoped()) {
            feedUsersPropertiesConditions.add(FEEDS_USERS_PROPERTIES.FUPR_USER_ID.eq(qCtx.userId()));
        }
        if (DSL.noCondition().equals(DSL.and(feedUsersPropertiesConditions))) {
            return Mono.error(() -> new IllegalArgumentException("No feed user properties condition"));
        }
        var deletePropertiesQuery = dsl.deleteQuery(FEEDS_USERS_PROPERTIES);
        deletePropertiesQuery.addConditions(feedUsersPropertiesConditions);

        Condition feedsConditions = qCtx.filter().accept(FeedConditionsVisitors.feedIdVisitor());
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
                    tx.dsl().execute(deletePropertiesQuery);
                    tx.dsl().execute(deleteFeedsErrorsQuery);
                    int unsubscribed = tx.dsl().execute(deleteUserLinkQuery);
                    int purged = deleteFeedQuery.map(q -> tx.dsl().execute(q)).orElse(0);
                    return new FeedDeletedResult(unsubscribed, purged);
                }));
    }

    @Override
    public Mono<Void> deleteUserRelations(String userId, Collection<String> feedsIds) {
        if (feedsIds.isEmpty()) {
            return Mono.empty().then();
        }
        requireNonNull(userId, "User ID is mandatory for this operation");
        return Mono.fromCallable(() -> dsl.delete(FEEDS_USERS).where(
                                FEEDS_USERS.FEUS_USER_ID.eq(userId),
                                FEEDS_USERS.FEUS_FEED_ID.in(feedsIds))
                        .execute())
                .subscribeOn(databaseScheduler)
                .then();
    }

    @Override
    public Mono<Void> deleteFeedProperties(String userId, Collection<String> feedsIds) {
        if (feedsIds.isEmpty()) {
            return Mono.empty().then();
        }
        requireNonNull(userId, "User ID is mandatory for this operation");
        return Mono.fromCallable(() -> dsl.delete(FEEDS_USERS_PROPERTIES).where(
                                FEEDS_USERS_PROPERTIES.FUPR_USER_ID.eq(userId),
                                FEEDS_USERS_PROPERTIES.FUPR_FEED_ID.in(feedsIds))
                        .execute())
                .subscribeOn(databaseScheduler)
                .then();
    }

    private Select<Record> buildSelectQuery(QueryContext qCtx) {
        List<String> filterProperties = qCtx.filter().accept(PROPERTIES_CRITERIA_VISITOR);
        Condition conditions = qCtx.filter().accept(JOOQ_CONDITION_VISITOR);
        SelectQuery<Record> select = dsl.selectQuery();
        select.addSelect(FEEDS.fields());
        select.addFrom(FEEDS);
        select.addConditions(conditions);
        select.addConditions(FEEDS.FEED_VISIBLE.isTrue());

        if (qCtx.isScoped()) {
            if (filterProperties.contains(TAGS)) {
                select.addJoin(FEEDS_USERS_PROPERTIES, JoinType.JOIN,
                        FEEDS_USERS_PROPERTIES.FUPR_FEED_ID.eq(FEEDS.FEED_ID).and(
                                FEEDS_USERS_PROPERTIES.FUPR_USER_ID.eq(qCtx.userId())));
            }
            select.addJoin(FEEDS_USERS, JoinType.JOIN,
                    FEEDS.FEED_ID.eq(FEEDS_USERS.FEUS_FEED_ID).and(FEEDS_USERS.FEUS_USER_ID.eq(qCtx.userId())));
        } else {
            if (filterProperties.contains(TAGS)) {
                select.addJoin(FEEDS_USERS_PROPERTIES, JoinType.JOIN,
                        FEEDS_USERS_PROPERTIES.FUPR_FEED_ID.eq(FEEDS.FEED_ID).and(
                                FEEDS_USERS_PROPERTIES.FUPR_USER_ID.isNull()));
            }
            select.addSelect(DSL.groupConcat(FEEDS_USERS.FEUS_USER_ID).as(FEEDS_USERS.FEUS_USER_ID));
            select.addJoin(FEEDS_USERS, JoinType.LEFT_OUTER_JOIN,
                    FEEDS.FEED_ID.eq(FEEDS_USERS.FEUS_FEED_ID));
            select.addGroupBy(FEEDS.fields());
            Condition havings = qCtx.filter().accept(FeedConditionsVisitors.feedUserHavingVisitor());
            select.addHaving(havings);
        }

        return JooqPagination.apply(qCtx.pagination(), FEEDS_PROPERTIES_MAPPING, select);
    }
}
