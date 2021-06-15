package fr.ght1pc9kc.baywatch.infra.adapters.persistence;

import fr.ght1pc9kc.baywatch.api.model.EntitiesProperties;
import fr.ght1pc9kc.baywatch.api.model.Feed;
import fr.ght1pc9kc.baywatch.domain.exceptions.BadCriteriaFilter;
import fr.ght1pc9kc.baywatch.domain.techwatch.model.QueryContext;
import fr.ght1pc9kc.baywatch.domain.techwatch.ports.FeedPersistencePort;
import fr.ght1pc9kc.baywatch.dsl.tables.records.FeedsRecord;
import fr.ght1pc9kc.baywatch.dsl.tables.records.FeedsUsersRecord;
import fr.ght1pc9kc.baywatch.infra.mappers.BaywatchMapper;
import fr.ght1pc9kc.baywatch.infra.mappers.PropertiesMappers;
import fr.ght1pc9kc.baywatch.infra.request.filter.PredicateSearchVisitor;
import fr.ght1pc9kc.juery.basic.filter.ListPropertiesCriteriaVisitor;
import fr.ght1pc9kc.juery.basic.filter.QueryStringFilterVisitor;
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
import java.util.Set;
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
    private static final ListPropertiesCriteriaVisitor PROPERTIES_CRITERIA_VISITOR = new ListPropertiesCriteriaVisitor();

    private final Scheduler databaseScheduler;
    private final DSLContext dsl;
    private final BaywatchMapper baywatchMapper;

    @Override
    public Mono<Feed> get(QueryContext qCtx) {
        return list(QueryContext.first(qCtx)).next();
    }

    @Override
    public Flux<Feed> list(QueryContext qCtx) {
        Condition conditions = qCtx.filter.accept(JOOQ_CONDITION_VISITOR);
        SelectQuery<Record> select = dsl.selectQuery();
        select.addSelect(FEEDS.fields());
        select.addConditions(conditions);

        if (qCtx.isScoped()) {
            select.addSelect(FEEDS_USERS.FEUS_TAGS);
            select.addJoin(FEEDS_USERS, JoinType.JOIN,
                    NEWS_FEEDS.NEFE_FEED_ID.eq(FEEDS_USERS.FEUS_FEED_ID).and(FEEDS_USERS.FEUS_USER_ID.eq(qCtx.userId)));
        }

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
    public Mono<Integer> delete(QueryContext qCtx) {
        Set<String> allowed = Set.of(EntitiesProperties.FEED_ID);
        if (!allowed.containsAll(qCtx.filter.accept(PROPERTIES_CRITERIA_VISITOR))) {
            return Mono.error(new BadCriteriaFilter(
                    String.format("Only %s allowed for filter deletion !", allowed)));
        }

        Condition condition = qCtx.filter.accept(JOOQ_CONDITION_VISITOR);

        var deleteUserLinkQuery = dsl.deleteQuery(FEEDS_USERS);
        deleteUserLinkQuery.addConditions(condition);
        if (qCtx.isScoped()) {
            deleteUserLinkQuery.addConditions(FEEDS_USERS.FEUS_USER_ID.eq(qCtx.userId));
        }

        qCtx.filter.accept(new QueryStringFilterVisitor())
        var deleteFeedQuery = dsl.deleteFrom(FEEDS)
                .where(FEEDS.FEED_ID.notIn(dsl.select(FEEDS_USERS.FEUS_FEED_ID).from(FEEDS_USERS).where(condition)))
                .and(FEEDS.FEED_ID.in());

        return Mono.fromCallable(() ->
                dsl.transactionResult(tx -> {
                    int countDeleted = tx.dsl().execute(deleteUserLinkQuery);
                    tx.dsl().execute(deleteFeedQuery);
                    return countDeleted;
                }));
    }
}
