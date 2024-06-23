package fr.ght1pc9kc.baywatch.security.domain;

import com.github.f4b6a3.ulid.UlidFactory;
import fr.ght1pc9kc.baywatch.common.api.model.UserMeta;
import fr.ght1pc9kc.baywatch.common.domain.QueryContext;
import fr.ght1pc9kc.baywatch.security.api.AuthenticationFacade;
import fr.ght1pc9kc.baywatch.security.api.AuthorizationService;
import fr.ght1pc9kc.baywatch.security.api.PasswordService;
import fr.ght1pc9kc.baywatch.security.api.UserService;
import fr.ght1pc9kc.baywatch.security.api.model.Permission;
import fr.ght1pc9kc.baywatch.security.api.model.Role;
import fr.ght1pc9kc.baywatch.security.api.model.RoleUtils;
import fr.ght1pc9kc.baywatch.security.api.model.User;
import fr.ght1pc9kc.baywatch.security.domain.exceptions.ConstraintViolationPersistenceException;
import fr.ght1pc9kc.baywatch.security.domain.exceptions.UnauthenticatedUser;
import fr.ght1pc9kc.baywatch.security.domain.exceptions.UnauthorizedOperation;
import fr.ght1pc9kc.baywatch.security.domain.exceptions.UserCreateException;
import fr.ght1pc9kc.baywatch.security.domain.ports.AuthorizationPersistencePort;
import fr.ght1pc9kc.baywatch.security.domain.ports.NotificationPort;
import fr.ght1pc9kc.baywatch.security.domain.ports.UserPersistencePort;
import fr.ght1pc9kc.entity.api.Entity;
import fr.ght1pc9kc.juery.api.Criteria;
import fr.ght1pc9kc.juery.api.PageRequest;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;

import static fr.ght1pc9kc.baywatch.common.api.DefaultMeta.NO_ONE;
import static fr.ght1pc9kc.baywatch.common.api.model.EntitiesProperties.ID;
import static fr.ght1pc9kc.baywatch.common.api.model.EntitiesProperties.ROLES;
import static fr.ght1pc9kc.baywatch.common.api.model.UserMeta.createdAt;
import static fr.ght1pc9kc.baywatch.common.api.model.UserMeta.createdBy;
import static fr.ght1pc9kc.baywatch.notify.api.model.EventType.USER_NOTIFICATION;
import static fr.ght1pc9kc.baywatch.security.api.model.RoleUtils.hasRole;
import static java.util.function.Predicate.not;

@Slf4j
@AllArgsConstructor
public final class UserServiceImpl implements UserService, AuthorizationService {
    private static final String ID_PREFIX = "US";
    private static final String AUTHENTICATION_NOT_FOUND = "Authentication not found !";
    private static final String UNAUTHORIZED_USER = "User unauthorized for the operation !";

    private final UserPersistencePort userRepository;
    private final AuthorizationPersistencePort authorizationRepository;
    private final NotificationPort notificationPort;
    private final AuthenticationFacade authFacade;
    private final PasswordService passwordService;
    private final Clock clock;
    private final UlidFactory idGenerator;

    @Override
    public Mono<Entity<User>> get(String userId) {
        return userRepository.get(userId)
                .flatMap(this::filterPublicData);
    }

    @Override
    public Flux<Entity<User>> list(PageRequest pageRequest) {
        return authFacade.getConnectedUser()
                .map(u -> QueryContext.from(pageRequest).withUserId(u.id()))
                .flatMapMany(userRepository::list)
                .flatMap(this::filterPublicData);
    }

    @Override
    public Mono<Integer> count(PageRequest pageRequest) {
        return authFacade.getConnectedUser()
                .map(u -> QueryContext.all(pageRequest.filter()).withUserId(u.id()))
                .flatMap(userRepository::count);
    }

    @Override
    public Mono<Entity<User>> create(User user) {
        String userId = String.format("%s%s", ID_PREFIX, idGenerator.create());
        Instant now = clock.instant();
        return passwordService.checkPasswordStrength(user)
                .flatMap(eval -> (eval.isSecure())
                        ? Mono.just(user.withPassword(passwordService.encode(user.password())))
                        : Mono.error(new IllegalArgumentException(eval.message())))

                .flatMap(withPassword -> authFacade.getConnectedUser()
                        .<String>handle((u, sink) -> {
                            if (hasRole(u.self(), Role.ADMIN)) {
                                sink.next(u.id());
                                return;
                            }
                            sink.error(new UnauthorizedOperation(UNAUTHORIZED_USER));
                        })
                        .switchIfEmpty(Mono.just(userId))

                        .map(currentUserId -> Entity.identify(withPassword)
                                .meta(createdAt, now)
                                .meta(createdBy, currentUserId)
                                .withId(userId)))

                .flatMap(entity -> userRepository.persist(List.of(entity)).single())
                .flatMap(ignore -> grants(userId, user.roles()))
                .onErrorMap(ConstraintViolationPersistenceException.class, e ->
                        new UserCreateException(
                                String.format("Unable to create User, %s unavailable !", e.getPropertyField()),
                                List.of(e.getPropertyField()), e))

                .flatMap(this::notifyAdmins);
    }

    private Mono<Entity<User>> notifyAdmins(Entity<User> newUser) {
        return userRepository.list(QueryContext.all(Criteria.property(ROLES).eq(Role.ADMIN.toString())))
                .filter(not(admin -> admin.id().equals(newUser.meta(createdBy).orElse(NO_ONE))))
                .map(admin -> notificationPort.send(admin.id(), USER_NOTIFICATION,
                        String.format("New user %s created by %s.", newUser.self().login(), newUser.meta(createdBy).orElse(NO_ONE))))
                .then(Mono.just(newUser));
    }

    @Override
    public Mono<Entity<User>> update(Entity<User> user) {
        return update(user, null);
    }

    @Override
    public Mono<Entity<User>> update(Entity<User> user, String currentPassword) {
        log.atDebug().addArgument(user.self().login()).log("Update user {}");
        Objects.requireNonNull(user);
        if (user.self().roles().isEmpty()) {
            throw new IllegalArgumentException("User must have at least 1 roles !");
        }
        if (user.self().password() != null && user.self().password().trim().length() < 4) {
            throw new IllegalArgumentException("Password must be stronger !");
        }
        return authorizeSelfData(user.id())
                .flatMap(u -> get(u.id()))
                .handle((u, sink) -> {
                    if (hasRole(u.self(), Role.ADMIN)) {
                        sink.next(u);
                    } else if (user.id().equals(u.id())
                            && Objects.nonNull(currentPassword)
                            && passwordService.matches(currentPassword, u.self().password())) {
                        sink.next(u);
                    } else {
                        sink.error(new UnauthorizedOperation(UNAUTHORIZED_USER));
                    }

                }).flatMap(u -> {
                    User checkedUser = (Objects.nonNull(user.self().password()))
                            ? user.self().withPassword(passwordService.encode(user.self().password()))
                            : user.self();

                    return authFacade.getClientInfoContext()
                            .map(clientInfo -> Entity.identify(checkedUser)
                                    .meta(UserMeta.loginIP, clientInfo.ip().toString())
                                    .meta(UserMeta.loginAt, clock.instant())
                                    .withId(user.id()))
                            .switchIfEmpty(Mono.just(user.convert(old -> checkedUser)));

                }).flatMap(userRepository::update);
    }

    @Override
    public Flux<Entity<User>> delete(Collection<String> ids) {
        return authorizeSelfData(ids)
                .flatMapMany(u -> userRepository.list(QueryContext.all(Criteria.property(ID).in(ids))))
                .switchIfEmpty(Flux.error(new NoSuchElementException(String.format("Unable to find users %s :", ids))))
                .collectList()
                .flatMapMany(users -> Flux.fromIterable(users)
                        .map(Entity::id)
                        .collectList()
                        .flatMap(userRepository::delete)
                        .thenMany(Flux.fromIterable(users)));
    }

    @Override
    public Mono<Entity<User>> grants(String grantedUserId, Collection<Permission> permissions) {
        return authFacade.getConnectedUser()
                .flatMap(currentUser -> {
                    var tobeVerified = new ArrayList<String>();
                    boolean selfGrant = currentUser.id().equals(grantedUserId);

                    for (Permission perm : permissions) {
                        if (!RoleUtils.hasPermission(currentUser.self(), perm)) {
                            if (perm.entity().isPresent() && selfGrant) {
                                tobeVerified.add(perm.toString());
                            } else {
                                return Mono.error(() -> new UnauthorizedOperation("Unauthorized grant operation !"));
                            }
                        }
                    }

                    return authorizationRepository.count(tobeVerified).flatMap(count ->
                            (count > 0)
                                    ? Mono.error(() -> new UnauthorizedOperation("Unauthorized grant operation !"))
                                    : Mono.just(currentUser));

                }).flatMap(currentUser -> userRepository.persist(
                        grantedUserId, permissions.stream().map(Permission::toString).distinct().toList()))

                // Do not grant if not loginIn
                .switchIfEmpty(userRepository.get(grantedUserId));
    }

    @Override
    public Mono<Void> revokes(Permission permission, Collection<String> userIds) {
        return authFacade.getConnectedUser().flatMap(currentUser -> {
            if ((userIds.size() == 1 && userIds.contains(currentUser.id())) || RoleUtils.hasRole(currentUser.self(), Role.ADMIN)) {
                return Mono.just(currentUser);
            } else {
                return Mono.error(() -> new UnauthorizedOperation("Unauthorized revoke operation !"));
            }
        }).flatMap(currentUser ->
                userRepository.delete(permission.toString(), userIds.stream().distinct().toList()));
    }

    @Override
    public Mono<Void> remove(Collection<Permission> permissions) {
        return authorizeAllData()
                .flatMap(currentUser -> authorizationRepository.remove(permissions));
    }

    @Override
    public Flux<String> listGrantedUsers(Permission permission) {
        return authorizeAllData().flatMapMany(ignored ->
                authorizationRepository.grantees(permission));
    }

    private Mono<Entity<User>> filterPublicData(Entity<User> original) {
        return authFacade.getConnectedUser()
                .switchIfEmpty(Mono.error(new UnauthenticatedUser(AUTHENTICATION_NOT_FOUND)))
                .filter(u -> (hasRole(u.self(), Role.ADMIN)
                        || (hasRole(u.self(), Role.USER) && original.id().equals(u.id()))))
                .map(u -> original)
                .switchIfEmpty(Mono.just(Entity.identify(original.self().toBuilder()
                                .clearRoles()
                                .password(null)
                                .mail(null)
                                .build())
                        .withId(original.id())));
    }

    private Mono<Entity<User>> authorizeSelfData(String id) {
        return authorizeSelfData(Collections.singleton(id));
    }

    private Mono<Entity<User>> authorizeSelfData(Collection<String> ids) {
        return authFacade.getConnectedUser()
                .switchIfEmpty(Mono.error(new UnauthenticatedUser(AUTHENTICATION_NOT_FOUND)))
                .handle((u, sink) -> {
                    if (hasRole(u.self(), Role.ADMIN)
                            || (hasRole(u.self(), Role.USER) && ids.size() == 1 && ids.contains(u.id()))) {
                        sink.next(u);
                        return;
                    }
                    sink.error(new UnauthorizedOperation(UNAUTHORIZED_USER));
                });
    }

    private Mono<Entity<User>> authorizeAllData() {
        return authFacade.getConnectedUser()
                .switchIfEmpty(Mono.error(new UnauthenticatedUser(AUTHENTICATION_NOT_FOUND)))
                .handle((u, sink) -> {
                    if (hasRole(u.self(), Role.ADMIN)) {
                        sink.next(u);
                        return;
                    }
                    sink.error(new UnauthorizedOperation(UNAUTHORIZED_USER));
                });
    }
}
