package fr.ght1pc9kc.baywatch.security.infra.persistence;

import fr.ght1pc9kc.baywatch.common.infra.DatabaseQualifier;
import fr.ght1pc9kc.baywatch.dsl.tables.records.UsersRolesRecord;
import fr.ght1pc9kc.baywatch.security.api.model.Permission;
import fr.ght1pc9kc.baywatch.security.domain.ports.AuthorizationPersistencePort;
import lombok.RequiredArgsConstructor;
import org.jooq.Cursor;
import org.jooq.DSLContext;
import org.jooq.Record1;
import org.jooq.Result;
import org.jooq.Select;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import static fr.ght1pc9kc.baywatch.dsl.tables.UsersRoles.USERS_ROLES;
import static java.util.function.Predicate.not;

@Repository
@RequiredArgsConstructor
@SuppressWarnings("BlockingMethodInNonBlockingContext")
public class AuthorizationRepository implements AuthorizationPersistencePort {
    private final @DatabaseQualifier Scheduler databaseScheduler;
    private final DSLContext dsl;

    @Override
    public Mono<Integer> count(Collection<String> permissions) {
        if (permissions.isEmpty()) {
            return Mono.just(0);
        }
        return Mono.fromCallable(() -> dsl.fetchCount(USERS_ROLES, USERS_ROLES.USRO_ROLE.in(permissions)))
                .subscribeOn(databaseScheduler);
    }

    @Override
    public Flux<Map.Entry<String, Set<Permission>>> list(Collection<String> userIds) {
        if (Objects.isNull(userIds)) {
            return Flux.error(() -> new IllegalArgumentException("User list can not be null !"));
        }
        if (userIds.isEmpty()) {
            return Flux.empty();
        }
        Select<UsersRolesRecord> query = dsl.selectFrom(USERS_ROLES)
                .where(USERS_ROLES.USRO_USER_ID.in(userIds))
                .orderBy(USERS_ROLES.USRO_USER_ID);

        return Flux.<UsersRolesRecord>create(sink -> {
                    Cursor<UsersRolesRecord> cursor = query.fetchLazy();
                    sink.onRequest(n -> {
                        int count = (int) n;
                        Result<UsersRolesRecord> rs = cursor.fetchNext(count);
                        rs.forEach(sink::next);
                        if (rs.size() < count) {
                            sink.complete();
                        }
                    }).onDispose(cursor::close);
                }).limitRate(Integer.MAX_VALUE - 1).subscribeOn(databaseScheduler)

                .bufferUntilChanged(r -> r.get(USERS_ROLES.USRO_USER_ID))
                .filter(not(List::isEmpty))
                .map(permsRecords -> {
                    String userId = permsRecords.get(0).get(USERS_ROLES.USRO_USER_ID);
                    Set<Permission> permissions = permsRecords.stream()
                            .map(r -> r.get(USERS_ROLES.USRO_ROLE))
                            .map(Permission::from)
                            .collect(Collectors.toUnmodifiableSet());
                    return Map.entry(userId, permissions);
                });
    }

    @Override
    public Flux<String> grantees(Permission permission) {
        Select<Record1<String>> query = dsl.select(USERS_ROLES.USRO_USER_ID).from(USERS_ROLES).where(USERS_ROLES.USRO_ROLE.eq(permission.toString()));
        return Flux.<Record1<String>>create(sink -> {
                    Cursor<Record1<String>> cursor = query.fetchLazy();
                    sink.onRequest(n -> {
                        int count = (int) n;
                        Result<Record1<String>> rs = cursor.fetchNext(count);
                        rs.forEach(sink::next);
                        if (rs.size() < count) {
                            sink.complete();
                        }
                    }).onDispose(cursor::close);
                }).limitRate(Integer.MAX_VALUE - 1).subscribeOn(databaseScheduler)
                .map(Record1::component1);
    }

    @Override
    public Mono<Void> remove(Collection<Permission> permissions) {
        Set<String> authorizations = permissions.stream().map(Permission::toString).collect(Collectors.toUnmodifiableSet());
        return Mono.fromCallable(() -> dsl.deleteFrom(USERS_ROLES).where(USERS_ROLES.USRO_ROLE.in(authorizations)).execute())
                .subscribeOn(databaseScheduler)
                .then();
    }
}
