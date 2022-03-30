package fr.ght1pc9kc.baywatch.techwatch.infra.persistence;

import fr.ght1pc9kc.baywatch.common.api.model.Entity;
import fr.ght1pc9kc.baywatch.common.infra.PredicateSearchVisitor;
import fr.ght1pc9kc.baywatch.common.infra.mappers.PropertiesMappers;
import fr.ght1pc9kc.baywatch.dsl.tables.records.NewsUserStateRecord;
import fr.ght1pc9kc.baywatch.techwatch.api.model.Flags;
import fr.ght1pc9kc.baywatch.techwatch.api.model.State;
import fr.ght1pc9kc.baywatch.techwatch.domain.model.QueryContext;
import fr.ght1pc9kc.baywatch.techwatch.domain.ports.StatePersistencePort;
import fr.ght1pc9kc.juery.jooq.filter.JooqConditionVisitor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jooq.Condition;
import org.jooq.Cursor;
import org.jooq.DSLContext;
import org.jooq.Result;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;

import static fr.ght1pc9kc.baywatch.dsl.tables.NewsUserState.NEWS_USER_STATE;

@Slf4j
@Repository
@RequiredArgsConstructor
public class StateRepository implements StatePersistencePort {
    public static final JooqConditionVisitor STATE_CONDITION_VISITOR =
            new JooqConditionVisitor(PropertiesMappers.STATE_PROPERTIES_MAPPING);

    private final Scheduler databaseScheduler;
    private final DSLContext dsl;

    @Override
    public Flux<Entity<State>> list(QueryContext queryContext) {
        Condition conditions = queryContext.getFilter().accept(STATE_CONDITION_VISITOR);
        PredicateSearchVisitor<State> predicateSearchVisitor = new PredicateSearchVisitor<>();
        return Flux.<NewsUserStateRecord>create(sink -> {
                    Cursor<NewsUserStateRecord> cursor = dsl.selectFrom(NEWS_USER_STATE).where(conditions).fetchLazy();
                    sink.onRequest(n -> {
                        int count = (int) n;
                        Result<NewsUserStateRecord> rs = cursor.fetchNext(count);
                        rs.forEach(sink::next);
                        if (rs.size() < count) {
                            sink.complete();
                        }
                    });
                }).limitRate(Integer.MAX_VALUE - 1).subscribeOn(databaseScheduler)
                .map(r -> Entity.identify(r.getNursNewsId(), State.of(r.getNursState())))
                .filter(s -> queryContext.getFilter().accept(predicateSearchVisitor).test(s.entity));
    }

    @Override
    public Mono<Integer> flag(String newsId, String userId, int flag) {
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
    public Mono<Integer> unflag(String newsId, String userId, int flag) {
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
}
