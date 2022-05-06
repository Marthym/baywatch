package fr.ght1pc9kc.baywatch.techwatch.infra.persistence;

import fr.ght1pc9kc.baywatch.common.api.model.Entity;
import fr.ght1pc9kc.baywatch.common.infra.mappers.PropertiesMappers;
import fr.ght1pc9kc.baywatch.dsl.tables.records.NewsUserStateRecord;
import fr.ght1pc9kc.baywatch.techwatch.api.model.Flags;
import fr.ght1pc9kc.baywatch.techwatch.api.model.State;
import fr.ght1pc9kc.baywatch.techwatch.domain.model.QueryContext;
import fr.ght1pc9kc.baywatch.techwatch.domain.ports.StatePersistencePort;
import fr.ght1pc9kc.juery.api.Criteria;
import fr.ght1pc9kc.juery.jooq.filter.JooqConditionVisitor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jooq.Cursor;
import org.jooq.DSLContext;
import org.jooq.Result;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;

import java.util.NoSuchElementException;

import static fr.ght1pc9kc.baywatch.common.api.model.EntitiesProperties.NEWS_ID;
import static fr.ght1pc9kc.baywatch.dsl.tables.NewsUserState.NEWS_USER_STATE;

@Slf4j
@Repository
@RequiredArgsConstructor
@SuppressWarnings("resource")
public class StateRepository implements StatePersistencePort {
    public static final JooqConditionVisitor STATE_CONDITION_VISITOR =
            new JooqConditionVisitor(PropertiesMappers.STATE_PROPERTIES_MAPPING);

    private final Scheduler databaseScheduler;
    private final DSLContext dsl;

    @Override
    public Mono<Entity<State>> get(QueryContext queryContext) {
        return list(queryContext).next();
    }

    @Override
    public Flux<Entity<State>> list(QueryContext queryContext) {
        var query = dsl.selectQuery(NEWS_USER_STATE);
        query.addConditions(queryContext.getFilter().accept(STATE_CONDITION_VISITOR));
        if (queryContext.isScoped()) {
            query.addConditions(NEWS_USER_STATE.NURS_USER_ID.eq(queryContext.getUserId()));
        }
        return Flux.<NewsUserStateRecord>create(sink -> {
                    Cursor<NewsUserStateRecord> cursor = query.fetchLazy();
                    sink.onRequest(n -> {
                        int count = (int) n;
                        Result<NewsUserStateRecord> rs = cursor.fetchNext(count);
                        rs.forEach(sink::next);
                        if (rs.size() < count) {
                            sink.complete();
                            cursor.close();
                        }
                    });
                }).limitRate(Integer.MAX_VALUE - 1).subscribeOn(databaseScheduler)
                .map(r -> Entity.identify(r.getNursNewsId(), r.getNursUserId(), State.of(r.getNursState())));
    }

    @Override
    public Mono<Entity<State>> flag(String newsId, String userId, int flag) {
        return Mono.fromCallable(() -> dsl.insertInto(NEWS_USER_STATE)
                        .columns(NEWS_USER_STATE.NURS_NEWS_ID, NEWS_USER_STATE.NURS_USER_ID, NEWS_USER_STATE.NURS_STATE)
                        .values(newsId, userId, Flags.NONE | flag)
                        .onDuplicateKeyUpdate()
                        .set(NEWS_USER_STATE.NURS_STATE, NEWS_USER_STATE.NURS_STATE.bitOr(flag))
                        .execute())
                .filter(updated -> updated > 0)
                .switchIfEmpty(Mono.error(NoSuchElementException::new))
                .flatMap(u -> get(QueryContext.first(Criteria.property(NEWS_ID).eq(newsId)).withUserId(userId)))
                .subscribeOn(databaseScheduler);
    }

    @Override
    public Mono<Entity<State>> unflag(String newsId, String userId, int flag) {
        return Mono.fromCallable(() -> dsl.insertInto(NEWS_USER_STATE)
                        .columns(NEWS_USER_STATE.NURS_NEWS_ID, NEWS_USER_STATE.NURS_USER_ID, NEWS_USER_STATE.NURS_STATE)
                        .values(newsId, userId, Flags.NONE)
                        .onDuplicateKeyUpdate()
                        .set(NEWS_USER_STATE.NURS_STATE, NEWS_USER_STATE.NURS_STATE.bitAnd(~flag))
                        .execute())
                .filter(updated -> updated > 0)
                .switchIfEmpty(Mono.error(NoSuchElementException::new))
                .flatMap(u -> get(QueryContext.first(Criteria.property(NEWS_ID).eq(newsId)).withUserId(userId)))
                .subscribeOn(databaseScheduler);
    }
}
