package fr.ght1pc9kc.baywatch.infra.adapters.persistence;

import com.machinezoo.noexception.Exceptions;
import fr.ght1pc9kc.baywatch.api.NewsPersistencePort;
import fr.ght1pc9kc.baywatch.api.model.News;
import fr.ght1pc9kc.baywatch.api.model.RawNews;
import fr.ght1pc9kc.baywatch.api.model.State;
import fr.ght1pc9kc.baywatch.api.model.search.Criteria;
import fr.ght1pc9kc.baywatch.dsl.tables.records.NewsFeedsRecord;
import fr.ght1pc9kc.baywatch.dsl.tables.records.NewsRecord;
import fr.ght1pc9kc.baywatch.infra.mappers.NewsToRecordConverter;
import fr.ght1pc9kc.baywatch.infra.search.JooqSearchVisitor;
import fr.ght1pc9kc.baywatch.infra.search.PredicateSearchVisitor;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jooq.*;
import org.springframework.core.convert.ConversionService;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;

import java.util.Collection;
import java.util.List;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import static fr.ght1pc9kc.baywatch.dsl.tables.News.NEWS;
import static fr.ght1pc9kc.baywatch.dsl.tables.NewsFeeds.NEWS_FEEDS;
import static fr.ght1pc9kc.baywatch.dsl.tables.NewsUserState.NEWS_USER_STATE;

@Slf4j
@Component
@AllArgsConstructor
public class NewsRepository implements NewsPersistencePort {
    private final Scheduler databaseScheduler;
    private final DSLContext dsl;
    private final ConversionService conversionService;

    @Override
    public Mono<Void> persist(Collection<News> toCreate) {
        List<NewsRecord> records = toCreate.stream()
                .map(n -> conversionService.convert(n, NewsRecord.class))
                .collect(Collectors.toList());

        List<NewsFeedsRecord> newsFeedsRecords = toCreate.stream()
                .map(n -> conversionService.convert(n, NewsFeedsRecord.class))
                .collect(Collectors.toList());

        return Mono.fromCallable(() ->
                dsl.loadInto(NEWS)
                        .batchAll()
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
                        dsl.loadInto(NEWS_FEEDS)
                                .batchAll()
                                .onDuplicateKeyIgnore()
                                .onErrorIgnore()
                                .loadRecords(newsFeedsRecords)
                                .fieldsCorresponding()
                                .execute()))
                .subscribeOn(databaseScheduler)
                .then();

    }

    @Override
    @SuppressWarnings("ReactiveStreamsNullableInLambdaInTransform")
    public Flux<News> userList(Criteria searchCriteria) {
        Condition conditions = searchCriteria.visit(new JooqSearchVisitor(NewsToRecordConverter.PROPERTIES_MAPPING::get));
        PredicateSearchVisitor predicateSearchVisitor = new PredicateSearchVisitor();
        return Flux.<Record>create(sink -> {
            Cursor<Record> cursor = dsl
                    .select(NEWS.fields()).select(NEWS_FEEDS.NEFE_FEED_ID).select(NEWS_USER_STATE.NURS_STATE)
                    .from(NEWS)
                    .leftJoin(NEWS_USER_STATE).on(NEWS.NEWS_ID.eq(NEWS_USER_STATE.NURS_NEWS_ID))
                    .leftJoin(NEWS_FEEDS).on(NEWS.NEWS_ID.eq(NEWS_FEEDS.NEFE_NEWS_ID))
                    .where(conditions)
                    .fetchLazy();
            sink.onRequest(n -> {
                int count = Long.valueOf(n).intValue();
                Result<Record> rs = cursor.fetchNext(count);
                rs.forEach(sink::next);
                if (rs.size() < count) {
                    sink.complete();
                }
            });
        }).limitRate(Integer.MAX_VALUE - 1).subscribeOn(databaseScheduler)
                .map(r -> conversionService.convert(r, News.class))
                .filter(searchCriteria.visit(predicateSearchVisitor));
    }

    @Override
    public Flux<RawNews> list(Criteria searchCriteria) {
        return null;
    }

    @Override
    public Flux<Entry<String, State>> listState(Criteria searchCriteria) {
        return null;
    }

    @Override
    public Mono<Integer> delete(Collection<String> ids) {
        return Mono.fromCallable(() ->
                dsl.transactionResult(tx -> {
                    DSLContext txDsl = tx.dsl();
                    txDsl.deleteFrom(NEWS_FEEDS).where(NEWS_FEEDS.NEFE_NEWS_ID.in(ids)).execute();
                    return txDsl.deleteFrom(NEWS).where(NEWS.NEWS_ID.in(ids)).execute();
                }));
    }
}
