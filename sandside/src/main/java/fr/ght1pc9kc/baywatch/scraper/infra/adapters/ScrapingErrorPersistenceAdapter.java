package fr.ght1pc9kc.baywatch.scraper.infra.adapters;

import fr.ght1pc9kc.baywatch.common.domain.QueryContext;
import fr.ght1pc9kc.baywatch.common.infra.DatabaseQualifier;
import fr.ght1pc9kc.baywatch.dsl.tables.records.FeedsErrorsRecord;
import fr.ght1pc9kc.baywatch.scraper.api.model.ScrapingError;
import fr.ght1pc9kc.baywatch.scraper.domain.ports.ScrapingErrorPersistencePort;
import fr.ght1pc9kc.baywatch.scraper.infra.config.ScraperMapper;
import fr.ght1pc9kc.entity.api.Entity;
import fr.ght1pc9kc.juery.api.Criteria;
import fr.ght1pc9kc.juery.jooq.filter.JooqConditionVisitor;
import lombok.RequiredArgsConstructor;
import org.jooq.Condition;
import org.jooq.Cursor;
import org.jooq.DSLContext;
import org.jooq.DeleteQuery;
import org.jooq.Query;
import org.jooq.Result;
import org.jooq.SelectQuery;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static fr.ght1pc9kc.baywatch.common.api.model.EntitiesProperties.ID;
import static fr.ght1pc9kc.baywatch.dsl.tables.FeedsErrors.FEEDS_ERRORS;

@Repository
@RequiredArgsConstructor
@SuppressWarnings("BlockingMethodInNonBlockingContext")
public class ScrapingErrorPersistenceAdapter implements ScrapingErrorPersistencePort {
    private static final JooqConditionVisitor JOOQ_CONDITION_VISITOR =
            new JooqConditionVisitor(ScraperMapper.FEEDS_ERRORS_PROPERTIES_MAPPING::get);

    private final DSLContext dsl;
    private final ScraperMapper mapper;
    private final @DatabaseQualifier Scheduler databaseScheduler;

    @Override
    public Flux<Entity<ScrapingError>> persist(Collection<Entity<ScrapingError>> errors) {
        List<Query> inserts = new ArrayList<>(errors.size());
        List<String> ids = new ArrayList<>(errors.size());
        for (Entity<ScrapingError> error : errors) {
            FeedsErrorsRecord feedErrorRecord = mapper.getFeedErrorRecord(error);
            FeedsErrorsRecord feedErrorsUpdateRecord = FEEDS_ERRORS.newRecord();
            feedErrorsUpdateRecord.from(feedErrorRecord,
                    FEEDS_ERRORS.FEER_LAST_STATUS, FEEDS_ERRORS.FEER_LAST_LABEL, FEEDS_ERRORS.FEER_LAST_TIME);
            inserts.add(dsl.insertInto(FEEDS_ERRORS).set(feedErrorRecord)
                    .onDuplicateKeyUpdate()
                    .set(feedErrorsUpdateRecord));
            ids.add(error.id());
        }

        return Mono.fromCallable(() -> dsl.batch(inserts).execute())
                .subscribeOn(databaseScheduler)
                .thenMany(list(QueryContext.all(Criteria.property(ID).in(ids))));
    }

    @Override
    public Flux<Entity<ScrapingError>> list(QueryContext query) {
        Condition conditions = query.filter.accept(JOOQ_CONDITION_VISITOR);
        SelectQuery<FeedsErrorsRecord> select = dsl.selectQuery(FEEDS_ERRORS);
        select.addConditions(conditions);

        return Flux.<FeedsErrorsRecord>create(sink -> {
                    Cursor<FeedsErrorsRecord> cursor = select.fetchLazy();
                    sink.onRequest(n -> {
                        Result<FeedsErrorsRecord> rs = cursor.fetchNext((int) n);
                        rs.forEach(sink::next);
                        if (rs.size() < n) {
                            sink.complete();
                        }
                    });
                }).limitRate(Integer.MAX_VALUE - 1)
                .subscribeOn(databaseScheduler)
                .map(mapper::getFeedErrorEntity);
    }

    @Override
    public Mono<Void> delete(QueryContext query) {
        Condition conditions = query.filter.accept(JOOQ_CONDITION_VISITOR);

        DeleteQuery<FeedsErrorsRecord> deleteQuery = dsl.deleteQuery(FEEDS_ERRORS);
        deleteQuery.addConditions(conditions);
        return Mono.from(deleteQuery).then();
    }
}
