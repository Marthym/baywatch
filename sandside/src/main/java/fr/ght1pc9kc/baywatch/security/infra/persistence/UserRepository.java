package fr.ght1pc9kc.baywatch.security.infra.persistence;

import fr.ght1pc9kc.baywatch.common.api.model.Entity;
import fr.ght1pc9kc.baywatch.common.infra.DatabaseQualifier;
import fr.ght1pc9kc.baywatch.common.infra.PredicateSearchVisitor;
import fr.ght1pc9kc.baywatch.common.infra.mappers.BaywatchMapper;
import fr.ght1pc9kc.baywatch.common.infra.mappers.PropertiesMappers;
import fr.ght1pc9kc.baywatch.dsl.tables.records.UsersRecord;
import fr.ght1pc9kc.baywatch.dsl.tables.records.UsersRolesRecord;
import fr.ght1pc9kc.baywatch.security.api.model.User;
import fr.ght1pc9kc.baywatch.security.domain.ports.UserPersistencePort;
import fr.ght1pc9kc.baywatch.techwatch.domain.model.QueryContext;
import fr.ght1pc9kc.juery.jooq.filter.JooqConditionVisitor;
import fr.ght1pc9kc.juery.jooq.pagination.JooqPagination;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jooq.Condition;
import org.jooq.Cursor;
import org.jooq.DSLContext;
import org.jooq.Query;
import org.jooq.Record;
import org.jooq.Result;
import org.jooq.Select;
import org.jooq.impl.DSL;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import static fr.ght1pc9kc.baywatch.common.infra.mappers.PropertiesMappers.USER_PROPERTIES_MAPPING;
import static fr.ght1pc9kc.baywatch.dsl.tables.FeedsUsers.FEEDS_USERS;
import static fr.ght1pc9kc.baywatch.dsl.tables.NewsUserState.NEWS_USER_STATE;
import static fr.ght1pc9kc.baywatch.dsl.tables.Users.USERS;
import static fr.ght1pc9kc.baywatch.dsl.tables.UsersRoles.USERS_ROLES;

@Slf4j
@Repository
@RequiredArgsConstructor
@SuppressWarnings("BlockingMethodInNonBlockingContext")
public class UserRepository implements UserPersistencePort {
    private static final JooqConditionVisitor JOOQ_CONDITION_VISITOR =
            new JooqConditionVisitor(PropertiesMappers.USER_PROPERTIES_MAPPING::get);
    private static final PredicateSearchVisitor<Entity<User>> USER_PREDICATE_VISITOR = new PredicateSearchVisitor<>();

    private final @DatabaseQualifier Scheduler databaseScheduler;
    private final DSLContext dsl;
    private final BaywatchMapper baywatchConverter;

    @Override
    public Mono<Entity<User>> get(String id) {
        return list(QueryContext.id(id)).next();
    }

    @Override
    @SuppressWarnings("resource")
    public Flux<Entity<User>> list(QueryContext qCtx) {
        Condition conditions = qCtx.filter.accept(JOOQ_CONDITION_VISITOR);
        Select<Record> select = JooqPagination.apply(
                qCtx.pagination, USER_PROPERTIES_MAPPING,
                dsl.select(USERS.fields()).select(DSL.groupConcat(USERS_ROLES.USRO_ROLE).as(USERS_ROLES.USRO_ROLE.getName()))
                        .from(USERS)
                        .join(USERS_ROLES).on(USERS_ROLES.USRO_USER_ID.eq(USERS.USER_ID))
                        .where(conditions)
                        .groupBy(USERS.fields()));

        return Flux.<Record>create(sink -> {
                    Cursor<Record> cursor = select.fetchLazy();
                    sink.onRequest(n -> {
                        int count = (int) n;
                        Result<Record> rs = cursor.fetchNext(count);
                        rs.forEach(sink::next);
                        if (rs.size() < count) {
                            sink.complete();
                        }
                    }).onDispose(cursor::close);
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
        List<UsersRecord> usersRecords = toPersist.stream()
                .map(baywatchConverter::entityUserToRecord)
                .toList();
        List<UsersRolesRecord> usersRolesRecords = toPersist.stream()
                .flatMap(e -> e.self.roles.stream()
                        .map(r -> USERS_ROLES.newRecord()
                                .setUsroUserId(e.id)
                                .setUsroRole(r))
                ).toList();

        return Mono.fromCallable(() -> dsl.transactionResult(tx -> {
                    int[] inserted = tx.dsl().batchInsert(usersRecords).execute();
                    tx.dsl().batchInsert(usersRolesRecords).execute();
                    return inserted;
                })).subscribeOn(databaseScheduler)

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
                    txDsl.deleteFrom(USERS_ROLES).where(USERS_ROLES.USRO_USER_ID.in(ids)).execute();
                    return txDsl.deleteFrom(USERS).where(USERS.USER_ID.in(ids)).execute();
                })).subscribeOn(databaseScheduler);
    }

    @Override
    public Mono<Entity<User>> persist(String userId, Collection<String> roles) {
        Query[] queries = roles.stream().map(role ->
                        dsl.insertInto(USERS_ROLES).columns(USERS_ROLES.USRO_USER_ID, USERS_ROLES.USRO_ROLE).values(userId, role))
                .toArray(Query[]::new);
        return Mono.fromCallable(() -> dsl.batch(queries).execute())
                .subscribeOn(databaseScheduler)
                .then(get(userId));
    }

    @Override
    public Mono<Entity<User>> delete(String userId, Collection<String> roles) {
        Query[] queries = roles.stream().map(role ->
                        dsl.deleteFrom(USERS_ROLES).where(USERS_ROLES.USRO_USER_ID.eq(userId).and(USERS_ROLES.USRO_ROLE.in(roles))))
                .toArray(Query[]::new);
        return Mono.fromCallable(() -> dsl.batch(queries).execute())
                .subscribeOn(databaseScheduler)
                .then(get(userId));
    }
}
