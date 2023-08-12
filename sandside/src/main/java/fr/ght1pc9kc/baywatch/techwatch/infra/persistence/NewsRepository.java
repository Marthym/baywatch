package fr.ght1pc9kc.baywatch.techwatch.infra.persistence;

import com.machinezoo.noexception.Exceptions;
import fr.ght1pc9kc.baywatch.common.api.model.EntitiesProperties;
import fr.ght1pc9kc.baywatch.common.infra.DatabaseQualifier;
import fr.ght1pc9kc.baywatch.common.infra.mappers.BaywatchMapper;
import fr.ght1pc9kc.baywatch.dsl.tables.records.NewsFeedsRecord;
import fr.ght1pc9kc.baywatch.dsl.tables.records.NewsRecord;
import fr.ght1pc9kc.baywatch.techwatch.api.model.Flags;
import fr.ght1pc9kc.baywatch.techwatch.api.model.News;
import fr.ght1pc9kc.baywatch.techwatch.domain.model.QueryContext;
import fr.ght1pc9kc.baywatch.techwatch.domain.ports.NewsPersistencePort;
import fr.ght1pc9kc.juery.basic.common.lang3.StringUtils;
import fr.ght1pc9kc.juery.basic.filter.ListPropertiesCriteriaVisitor;
import fr.ght1pc9kc.juery.jooq.filter.JooqConditionVisitor;
import fr.ght1pc9kc.juery.jooq.pagination.JooqPagination;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jooq.Condition;
import org.jooq.Cursor;
import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.JoinType;
import org.jooq.Operator;
import org.jooq.Record;
import org.jooq.Result;
import org.jooq.Select;
import org.jooq.SelectQuery;
import org.jooq.impl.DSL;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static fr.ght1pc9kc.baywatch.common.infra.mappers.PropertiesMappers.NEWS_PROPERTIES_MAPPING;
import static fr.ght1pc9kc.baywatch.common.infra.mappers.PropertiesMappers.NEWS_STATE;
import static fr.ght1pc9kc.baywatch.common.infra.mappers.PropertiesMappers.POPULAR;
import static fr.ght1pc9kc.baywatch.dsl.tables.News.NEWS;
import static fr.ght1pc9kc.baywatch.dsl.tables.NewsFeeds.NEWS_FEEDS;
import static fr.ght1pc9kc.baywatch.dsl.tables.NewsUserState.NEWS_USER_STATE;

@Slf4j
@Repository
@AllArgsConstructor
@SuppressWarnings("resource")
public class NewsRepository implements NewsPersistencePort {
    public static final JooqConditionVisitor NEWS_CONDITION_VISITOR =
            new JooqConditionVisitor(NEWS_PROPERTIES_MAPPING);
    private static final ListPropertiesCriteriaVisitor LIST_PROPERTIES_VISITOR = new ListPropertiesCriteriaVisitor();

    private final @DatabaseQualifier Scheduler databaseScheduler;
    private final DSLContext dsl;
    private final BaywatchMapper baywatchMapper;

    @Override
    public Mono<News> get(QueryContext qCtx) {
        return list(qCtx).next();
    }

    @Override
    public Flux<News> list(QueryContext qCtx) {
        final Select<Record> query = buildSelectQuery(List.of(NEWS.fields()), qCtx, dsl);

        return Flux.<Record>create(sink -> {
                    Cursor<Record> cursor = query.fetchLazy();
                    sink.onRequest(n -> {
                        int count = (int) n;
                        Result<Record> rs = cursor.fetchNext(count);
                        rs.forEach(sink::next);
                        if (rs.size() < count) {
                            sink.complete();
                            cursor.close();
                        }
                    });
                }).limitRate(Integer.MAX_VALUE - 1).subscribeOn(databaseScheduler)
                .map(baywatchMapper::recordToNews);
    }

    @Override
    public Flux<String> listId(QueryContext qCtx) {
        final Select<Record> query = buildSelectQuery(List.of(NEWS.NEWS_ID), qCtx, dsl);

        return Flux.<String>create(sink -> {
            Cursor<Record> cursor = query.fetchLazy();
            sink.onRequest(n -> {
                int count = (int) n;
                Result<Record> rs = cursor.fetchNext(count);
                rs.forEach(r -> sink.next(r.get(NEWS.NEWS_ID)));
                if (rs.size() < count) {
                    sink.complete();
                    cursor.close();
                }
            });
        }).limitRate(Integer.MAX_VALUE - 1).subscribeOn(databaseScheduler);
    }

    @Override
    public Mono<Integer> persist(Collection<News> toCreate) {
        List<NewsRecord> records = toCreate.stream()
                .map(baywatchMapper::newsToNewsRecord)
                .toList();

        List<NewsFeedsRecord> newsFeedsRecords = toCreate.stream()
                .flatMap(NewsRepository::toNewsFeedsRecords)
                .toList();

        return Mono.fromCallable(() ->
                        dsl.loadInto(NEWS).batchAll()
                                .onDuplicateKeyIgnore()
                                .onErrorIgnore()
                                .loadRecords(records)
                                .fieldsCorresponding()
                                .execute())
                .subscribeOn(databaseScheduler)
                .map(Exceptions.wrap().function(x -> {
                    dsl.loadInto(NEWS_FEEDS).batchAll()
                            .onDuplicateKeyIgnore()
                            .onErrorIgnore()
                            .loadRecords(newsFeedsRecords)
                            .fieldsCorresponding()
                            .execute();
                    return x;
                }))
                .subscribeOn(databaseScheduler)
                .flatMap(loader -> Mono.fromCallable(() -> dsl.execute("ANALYZE"))
                        .subscribeOn(databaseScheduler)
                        .thenReturn(loader))
                .map(loader -> {
                    log.debug("Load {} News with {} error(s).", loader.processed(), loader.errors().size());
                    return loader.processed();
                });
    }

    @Override
    public Mono<Integer> delete(Collection<String> ids) {
        return Mono.fromCallable(() ->
                dsl.transactionResult(tx -> {
                    DSLContext txDsl = tx.dsl();
                    txDsl.deleteFrom(NEWS_FEEDS).where(NEWS_FEEDS.NEFE_NEWS_ID.in(ids)).execute();
                    txDsl.deleteFrom(NEWS_USER_STATE).where(NEWS_USER_STATE.NURS_NEWS_ID.in(ids)).execute();
                    return txDsl.deleteFrom(NEWS).where(NEWS.NEWS_ID.in(ids)).execute();
                })).subscribeOn(databaseScheduler);
    }

    @Override
    public Mono<Integer> count(QueryContext qCtx) {
        SelectQuery<Record> select = buildSelectQuery(List.of(NEWS.NEWS_ID), qCtx, dsl);
        return Mono.fromCallable(() -> dsl.fetchCount(select))
                .subscribeOn(databaseScheduler);
    }

    private static SelectQuery<Record> buildSelectQuery(Collection<Field<?>> fields, QueryContext qCtx, DSLContext dsl) {
        Condition conditions = Optional.ofNullable(qCtx.filter).map(f -> f.accept(NEWS_CONDITION_VISITOR))
                .orElse(DSL.noCondition());

        List<String> properties = Optional.ofNullable(qCtx.filter).map(f -> f.accept(LIST_PROPERTIES_VISITOR))
                .orElse(List.of());

        SelectQuery<Record> select = dsl.selectQuery();
        select.addSelect(fields);
        select.addFrom(NEWS);
        select.addConditions(conditions);

        select.addSelect(DSL.groupConcat(NEWS_FEEDS.NEFE_FEED_ID).as(NEWS_FEEDS.NEFE_FEED_ID));
        select.addJoin(NEWS_FEEDS, JoinType.LEFT_OUTER_JOIN, NEWS.NEWS_ID.eq(NEWS_FEEDS.NEFE_NEWS_ID));
        select.addGroupBy(fields);

        if (!StringUtils.isBlank(qCtx.userId)) {
            select.addSelect(NEWS_STATE.NURS_STATE);
            select.addJoin(NEWS_STATE, JoinType.LEFT_OUTER_JOIN,
                    NEWS.NEWS_ID.eq(NEWS_STATE.NURS_NEWS_ID).and(NEWS_STATE.NURS_USER_ID.eq(qCtx.userId)));

            if (properties.contains(EntitiesProperties.POPULAR)) {
                ArrayList<Condition> popularJoinConditions = new ArrayList<>();
                popularJoinConditions.add(NEWS.NEWS_ID.eq(POPULAR.NURS_NEWS_ID));
                popularJoinConditions.add(DSL.coalesce(POPULAR.NURS_STATE, Flags.NONE).bitAnd(Flags.SHARED).eq(Flags.SHARED));
                if (!qCtx.teamMates.isEmpty()) {
                    popularJoinConditions.add(POPULAR.NURS_USER_ID.in(qCtx.teamMates));
                }
                select.addJoin(POPULAR, JoinType.LEFT_OUTER_JOIN, DSL.condition(Operator.AND, popularJoinConditions));
            }
        }

        SelectQuery<Record> paginateSelect = (SelectQuery<Record>) JooqPagination.apply(qCtx.pagination, NEWS_PROPERTIES_MAPPING, select);
        paginateSelect.addOrderBy(NEWS.NEWS_ID); // This avoids random order for records with same value in ordered fields
        return paginateSelect;
    }

    private static Stream<NewsFeedsRecord> toNewsFeedsRecords(News news) {
        return news.getFeeds().stream()
                .map(f -> NEWS_FEEDS.newRecord()
                        .setNefeNewsId(news.id())
                        .setNefeFeedId(f));
    }
}
