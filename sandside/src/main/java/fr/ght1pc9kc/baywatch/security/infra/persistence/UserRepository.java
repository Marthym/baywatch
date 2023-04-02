package fr.ght1pc9kc.baywatch.security.infra.persistence;

import fr.ght1pc9kc.baywatch.common.api.model.Entity;
import fr.ght1pc9kc.baywatch.common.infra.DatabaseQualifier;
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
import org.jetbrains.annotations.NotNull;
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

    private final @DatabaseQualifier Scheduler databaseScheduler;
    private final DSLContext dsl;
    private final BaywatchMapper baywatchConverter;

    @Override
    public Mono<Entity<User>> get(String id) {
        return list(QueryContext.id(id)).next();
    }

    @Override
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
                .map(baywatchConverter::recordToUserEntity);
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

        return Mono.fromCallable(() -> dsl.transactionResult(tx -> tx.dsl().batchInsert(usersRecords).execute()))
                .subscribeOn(databaseScheduler)
                .flatMapMany(insertedCount -> {
                    log.debug("{} user(s) inserted successfully.", Arrays.stream(insertedCount).sum());
                    return Flux.fromIterable(toPersist);
                });
    }

    @Override
    public Mono<Entity<User>> update(@NotNull String id, User user) {
        UsersRecord usersRecord = baywatchConverter.entityUserToRecord(Entity.identify(id, user));
        List<UsersRolesRecord> usersRolesRecords = user.roles.stream()
                .distinct()
                .map(r -> USERS_ROLES.newRecord().setUsroUserId(id).setUsroRole(r.toString()))
                .toList();
        return Mono.fromCallable(() -> dsl.transactionResult(tx -> {
                    tx.dsl().deleteFrom(USERS_ROLES).where(USERS_ROLES.USRO_USER_ID.eq(id)).execute();
                    if (!usersRolesRecords.isEmpty()) {
                        tx.dsl().batchInsert(usersRolesRecords).execute();
                    }
                    return tx.dsl().executeUpdate(usersRecord);
                })).subscribeOn(databaseScheduler)

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
                dsl.insertInto(USERS_ROLES)
                        .columns(USERS_ROLES.USRO_USER_ID, USERS_ROLES.USRO_ROLE)
                        .values(userId, role)
                        .onDuplicateKeyIgnore()
        ).toArray(Query[]::new);
        return Mono.fromCallable(() -> dsl.batch(queries).execute())
                .subscribeOn(databaseScheduler)
                .then(get(userId));
    }

    @Override
    public Mono<Void> delete(String role, Collection<String> userIds) {
        return Mono.fromCallable(() -> dsl.deleteFrom(USERS_ROLES)
                        .where(USERS_ROLES.USRO_USER_ID.in(userIds)
                                .and(USERS_ROLES.USRO_ROLE.eq(role)))
                        .execute())
                .subscribeOn(databaseScheduler)
                .then();
    }
}
