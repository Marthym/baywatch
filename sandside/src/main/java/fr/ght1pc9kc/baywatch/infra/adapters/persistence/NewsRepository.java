package fr.ght1pc9kc.baywatch.infra.adapters.persistence;

import com.machinezoo.noexception.Exceptions;
import fr.ght1pc9kc.baywatch.api.model.Flags;
import fr.ght1pc9kc.baywatch.api.model.News;
import fr.ght1pc9kc.baywatch.api.model.State;
import fr.ght1pc9kc.baywatch.domain.techwatch.model.QueryContext;
import fr.ght1pc9kc.baywatch.domain.techwatch.ports.NewsPersistencePort;
import fr.ght1pc9kc.baywatch.dsl.tables.records.NewsFeedsRecord;
import fr.ght1pc9kc.baywatch.dsl.tables.records.NewsRecord;
import fr.ght1pc9kc.baywatch.dsl.tables.records.NewsUserStateRecord;
import fr.ght1pc9kc.baywatch.infra.http.filter.PredicateSearchVisitor;
import fr.ght1pc9kc.baywatch.infra.mappers.BaywatchMapper;
import fr.ght1pc9kc.baywatch.infra.mappers.PropertiesMappers;
import fr.ght1pc9kc.juery.api.Criteria;
import fr.ght1pc9kc.juery.basic.common.lang3.StringUtils;
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
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static fr.ght1pc9kc.baywatch.dsl.tables.FeedsUsers.FEEDS_USERS;
import static fr.ght1pc9kc.baywatch.dsl.tables.News.NEWS;
import static fr.ght1pc9kc.baywatch.dsl.tables.NewsFeeds.NEWS_FEEDS;
import static fr.ght1pc9kc.baywatch.dsl.tables.NewsUserState.NEWS_USER_STATE;
import static fr.ght1pc9kc.baywatch.infra.mappers.PropertiesMappers.NEWS_PROPERTIES_MAPPING;

@Slf4j
@Repository
@AllArgsConstructor
public class NewsRepository implements NewsPersistencePort {
    public static final JooqConditionVisitor NEWS_CONDITION_VISITOR =
            new JooqConditionVisitor(NEWS_PROPERTIES_MAPPING);
    public static final JooqConditionVisitor STATE_CONDITION_VISITOR =
            new JooqConditionVisitor(PropertiesMappers.STATE_PROPERTIES_MAPPING);

    private final Scheduler databaseScheduler;
    private final DSLContext dsl;
    private final BaywatchMapper baywatchMapper;

    @Override
    public Mono<News> get(QueryContext qCtx) {
        return list(qCtx).next();
    }

    @Override
    public Flux<News> list(QueryContext qCtx) {
        PredicateSearchVisitor<News> predicateSearchVisitor = new PredicateSearchVisitor<>();

        final Select<Record> query = buildSelectQuery(qCtx);

        return Flux.<Record>create(sink -> {
            Cursor<Record> cursor = query.fetchLazy();
            sink.onRequest(n -> {
                int count = Long.valueOf(n).intValue();
                Result<Record> rs = cursor.fetchNext(count);
                rs.forEach(sink::next);
                if (rs.size() < count) {
                    sink.complete();
                }
            });
        }).limitRate(Integer.MAX_VALUE - 1).subscribeOn(databaseScheduler)
                .map(baywatchMapper::recordToNews)
                .filter(qCtx.filter.accept(predicateSearchVisitor));
    }

    @Override
    public Mono<Void> persist(Collection<News> toCreate) {
        List<NewsRecord> records = toCreate.stream()
                .map(baywatchMapper::newsToNewsRecord)
                .collect(Collectors.toList());

        List<NewsFeedsRecord> newsFeedsRecords = toCreate.stream()
                .flatMap(NewsRepository::toNewsFeedsRecords)
                .collect(Collectors.toList());

        return Mono.fromCallable(() ->
                dsl.loadInto(NEWS).batchAll()
                        .onDuplicateKeyIgnore()
                        .onErrorIgnore()
                        .loadRecords(records)
                        .fieldsCorresponding()
                        .execute())
                .subscribeOn(databaseScheduler)
                .map(loader -> {
                    log.info("Load {} News with {} error(s) and {} ignored",
                            loader.processed(), loader.errors().size(), loader.ignored());
                    return loader;
                })
                .map(Exceptions.wrap().function(x ->
                        dsl.loadInto(NEWS_FEEDS).batchAll()
                                .onDuplicateKeyIgnore()
                                .onErrorIgnore()
                                .loadRecords(newsFeedsRecords)
                                .fieldsCorresponding()
                                .execute()))
                .subscribeOn(databaseScheduler)
                .then();

    }

    @Override
    public Flux<Entry<String, State>> listState(Criteria searchCriteria) {
        Condition conditions = searchCriteria.accept(STATE_CONDITION_VISITOR);
        PredicateSearchVisitor<State> predicateSearchVisitor = new PredicateSearchVisitor<>();
        return Flux.<NewsUserStateRecord>create(sink -> {
            Cursor<NewsUserStateRecord> cursor = dsl.selectFrom(NEWS_USER_STATE).where(conditions).fetchLazy();
            sink.onRequest(n -> {
                int count = Long.valueOf(n).intValue();
                Result<NewsUserStateRecord> rs = cursor.fetchNext(count);
                rs.forEach(sink::next);
                if (rs.size() < count) {
                    sink.complete();
                }
            });
        }).limitRate(Integer.MAX_VALUE - 1).subscribeOn(databaseScheduler)
                .map(r -> Map.entry(r.getNursNewsId(), State.of(r.getNursState())))
                .filter(s -> searchCriteria.accept(predicateSearchVisitor).test(s.getValue()));
    }

    @Override
    public Mono<Integer> addStateFlag(String newsId, String userId, int flag) {
        return Mono.fromCallable(() -> dsl.insertInto(NEWS_USER_STATE)
                .columns(NEWS_USER_STATE.NURS_NEWS_ID, NEWS_USER_STATE.NURS_USER_ID, NEWS_USER_STATE.NURS_STATE)
                .values(newsId, userId, Flags.NONE | flag)
                .onDuplicateKeyUpdate()
                .set(NEWS_USER_STATE.NURS_STATE, NEWS_USER_STATE.NURS_STATE.bitOr(flag))
                .returning()
                .execute())
                .subscribeOn(databaseScheduler);
    }

    @Override
    public Mono<Integer> removeStateFlag(String newsId, String userId, int flag) {
        final int mask = ~(1 << (flag - 1));
        return Mono.fromCallable(() -> dsl.insertInto(NEWS_USER_STATE)
                .columns(NEWS_USER_STATE.NURS_NEWS_ID, NEWS_USER_STATE.NURS_USER_ID, NEWS_USER_STATE.NURS_STATE)
                .values(newsId, userId, Flags.NONE)
                .onDuplicateKeyUpdate()
                .set(NEWS_USER_STATE.NURS_STATE, NEWS_USER_STATE.NURS_STATE.bitAnd(mask))
                .returning()
                .execute())
                .subscribeOn(databaseScheduler);
    }

    @Override
    public Mono<Integer> deleteFeedLink(Collection<String> ids) {
        return Mono.fromCallable(() ->
                dsl.deleteFrom(NEWS_FEEDS).where(NEWS_FEEDS.NEFE_NEWS_ID.in(ids)).execute())
                .subscribeOn(databaseScheduler);
    }

    @Override
    public Mono<Integer> delete(Collection<String> ids) {
        return deleteFeedLink(ids)
                .then(Mono.fromCallable(() ->
                        dsl.transactionResult(tx -> {
                            DSLContext txDsl = tx.dsl();
                            txDsl.deleteFrom(NEWS_USER_STATE).where(NEWS_USER_STATE.NURS_NEWS_ID.in(ids)).execute();
                            return txDsl.deleteFrom(NEWS).where(NEWS.NEWS_ID.in(ids)).execute();
                        })).subscribeOn(databaseScheduler)
                );
    }

    @Override
    public Mono<Integer> count(QueryContext qCtx) {
        SelectQuery<Record> select = buildSelectQuery(qCtx);
        return Mono.fromCallable(() -> dsl.fetchCount(select))
                .subscribeOn(databaseScheduler);
    }

    private SelectQuery<Record> buildSelectQuery(QueryContext qCtx) {
        Condition conditions = qCtx.filter.accept(NEWS_CONDITION_VISITOR);

        SelectQuery<Record> select = dsl.selectQuery();
        select.addSelect(NEWS.fields());
        select.addFrom(NEWS);
        select.addConditions(conditions);

        select.addSelect(DSL.arrayAggDistinct(NEWS_FEEDS.NEFE_FEED_ID).as(NEWS_FEEDS.NEFE_FEED_ID));
        select.addJoin(NEWS_FEEDS, JoinType.LEFT_OUTER_JOIN, NEWS.NEWS_ID.eq(NEWS_FEEDS.NEFE_NEWS_ID));
        select.addGroupBy(NEWS.fields());

        if (!StringUtils.isBlank(qCtx.userId)) {
            select.addSelect(DSL.arrayAggDistinct(FEEDS_USERS.FEUS_TAGS).as(FEEDS_USERS.FEUS_TAGS));
            select.addJoin(FEEDS_USERS, JoinType.JOIN,
                    NEWS_FEEDS.NEFE_FEED_ID.eq(FEEDS_USERS.FEUS_FEED_ID).and(FEEDS_USERS.FEUS_USER_ID.eq(qCtx.userId)));

            select.addSelect(NEWS_USER_STATE.NURS_STATE);
            select.addJoin(NEWS_USER_STATE, JoinType.LEFT_OUTER_JOIN,
                    NEWS.NEWS_ID.eq(NEWS_USER_STATE.NURS_NEWS_ID).and(NEWS_USER_STATE.NURS_USER_ID.eq(qCtx.userId)));
        }

        return (SelectQuery<Record>) JooqPagination.apply(qCtx.pagination, NEWS_PROPERTIES_MAPPING, select);
    }

    private static Stream<NewsFeedsRecord> toNewsFeedsRecords(News news) {
        return news.getFeeds().stream()
                .map(f -> NEWS_FEEDS.newRecord()
                        .setNefeNewsId(news.getId())
                        .setNefeFeedId(f));
    }
}
