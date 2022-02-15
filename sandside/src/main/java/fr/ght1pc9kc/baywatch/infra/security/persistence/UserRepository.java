package fr.ght1pc9kc.baywatch.infra.security.persistence;

import fr.ght1pc9kc.baywatch.api.common.model.Entity;
import fr.ght1pc9kc.baywatch.api.security.model.User;
import fr.ght1pc9kc.baywatch.domain.ports.UserPersistencePort;
import fr.ght1pc9kc.baywatch.domain.techwatch.model.QueryContext;
import fr.ght1pc9kc.baywatch.dsl.tables.records.UsersRecord;
import fr.ght1pc9kc.baywatch.infra.common.mappers.BaywatchMapper;
import fr.ght1pc9kc.baywatch.infra.common.mappers.PropertiesMappers;
import fr.ght1pc9kc.baywatch.infra.http.filter.PredicateSearchVisitor;
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

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import static fr.ght1pc9kc.baywatch.dsl.tables.FeedsUsers.FEEDS_USERS;
import static fr.ght1pc9kc.baywatch.dsl.tables.NewsUserState.NEWS_USER_STATE;
import static fr.ght1pc9kc.baywatch.dsl.tables.Users.USERS;

@Slf4j
@Repository
@RequiredArgsConstructor
public class UserRepository implements UserPersistencePort {
    private static final JooqConditionVisitor JOOQ_CONDITION_VISITOR =
            new JooqConditionVisitor(PropertiesMappers.USER_PROPERTIES_MAPPING::get);
    private static final PredicateSearchVisitor<Entity<User>> USER_PREDICATE_VISITOR = new PredicateSearchVisitor<>();

    private final Scheduler databaseScheduler;
    private final DSLContext dsl;
    private final BaywatchMapper baywatchConverter;

    @Override
    public Mono<Entity<User>> get(String id) {
        return list(QueryContext.id(id)).next();
    }

    @Override
    public Flux<Entity<User>> list(QueryContext qCtx) {
        Condition conditions = qCtx.filter.accept(JOOQ_CONDITION_VISITOR);

        return Flux.<UsersRecord>create(sink -> {
                    Cursor<UsersRecord> cursor = dsl.selectFrom(USERS).where(conditions).fetchLazy();
                    sink.onRequest(n -> {
                        int count = Long.valueOf(n).intValue();
                        Result<UsersRecord> rs = cursor.fetchNext(count);
                        rs.forEach(sink::next);
                        if (rs.size() < count) {
                            sink.complete();
                        }
                    });
                }).limitRate(Integer.MAX_VALUE - 1).subscribeOn(databaseScheduler)
                .map(baywatchConverter::recordToUserEntity)
                .filter(qCtx.filter.accept(USER_PREDICATE_VISITOR));
    }

    @Override
    public Flux<Entity<User>> list() {
        return list(QueryContext.empty());
    }

    @Override
    public Mono<Integer> count(QueryContext qCtx) {
        Condition conditions = qCtx.getFilter().accept(JOOQ_CONDITION_VISITOR);
        return Mono.fromCallable(() -> dsl.fetchCount(dsl.selectFrom(USERS).where(conditions)))
                .subscribeOn(databaseScheduler);
    }

    @Override
    public Flux<Entity<User>> persist(Collection<Entity<User>> toPersist) {
        List<UsersRecord> records = toPersist.stream()
                .map(baywatchConverter::entityUserToRecord)
                .collect(Collectors.toList());

        return Mono.fromCallable(() ->
                        dsl.transactionResult(tx -> tx.dsl().batchInsert(records).execute()))
                .subscribeOn(databaseScheduler)

                .flatMapMany(insertedCount -> {
                    log.debug("{} user(s) inserted successfully.", Arrays.stream(insertedCount).sum());
                    return Flux.fromIterable(toPersist);
                });
    }

    @Override
    public Mono<Entity<User>> update(String id, User user) {
        UsersRecord usersRecord = baywatchConverter.entityUserToRecord(Entity.identify(id, user));
        return Mono.fromCallable(() -> dsl.executeUpdate(usersRecord))
                .subscribeOn(databaseScheduler)
                .then(get(id).subscribeOn(databaseScheduler));
    }

    @Override
    public Mono<Integer> delete(Collection<String> ids) {
        return Mono.fromCallable(() ->
                dsl.transactionResult(tx -> {
                    DSLContext txDsl = tx.dsl();
                    txDsl.deleteFrom(FEEDS_USERS).where(FEEDS_USERS.FEUS_USER_ID.in(ids)).execute();
                    txDsl.deleteFrom(NEWS_USER_STATE).where(NEWS_USER_STATE.NURS_USER_ID.in(ids)).execute();
                    return txDsl.deleteFrom(USERS).where(USERS.USER_ID.in(ids)).execute();
                })).subscribeOn(databaseScheduler);
    }
}
