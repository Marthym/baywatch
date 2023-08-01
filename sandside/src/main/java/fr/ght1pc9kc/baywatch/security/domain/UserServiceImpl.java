package fr.ght1pc9kc.baywatch.security.domain;

import com.github.f4b6a3.ulid.UlidFactory;
import fr.ght1pc9kc.baywatch.common.api.model.Entity;
import fr.ght1pc9kc.baywatch.security.api.AuthenticationFacade;
import fr.ght1pc9kc.baywatch.security.api.AuthorizationService;
import fr.ght1pc9kc.baywatch.security.api.UserService;
import fr.ght1pc9kc.baywatch.security.api.model.Permission;
import fr.ght1pc9kc.baywatch.security.api.model.Role;
import fr.ght1pc9kc.baywatch.security.api.model.RoleUtils;
import fr.ght1pc9kc.baywatch.security.api.model.User;
import fr.ght1pc9kc.baywatch.security.domain.exceptions.UnauthenticatedUser;
import fr.ght1pc9kc.baywatch.security.domain.exceptions.UnauthorizedOperation;
import fr.ght1pc9kc.baywatch.security.domain.model.PasswordStrength;
import fr.ght1pc9kc.baywatch.security.domain.ports.AuthorizationPersistencePort;
import fr.ght1pc9kc.baywatch.security.domain.ports.PasswordStrengthChecker;
import fr.ght1pc9kc.baywatch.security.domain.ports.UserPersistencePort;
import fr.ght1pc9kc.baywatch.techwatch.domain.model.QueryContext;
import fr.ght1pc9kc.juery.api.Criteria;
import fr.ght1pc9kc.juery.api.PageRequest;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.NoSuchElementException;
import java.util.Objects;

import static fr.ght1pc9kc.baywatch.common.api.model.EntitiesProperties.ID;
import static fr.ght1pc9kc.baywatch.security.api.model.RoleUtils.hasRole;

@Slf4j
@AllArgsConstructor
public final class UserServiceImpl implements UserService, AuthorizationService {
    private static final String ID_PREFIX = "US";
    private static final String AUTHENTICATION_NOT_FOUND = "Authentication not found !";

    private final UserPersistencePort userRepository;
    private final AuthorizationPersistencePort authorizationRepository;
    private final AuthenticationFacade authFacade;
    private final PasswordEncoder passwordEncoder;
    private final Clock clock;
    private final UlidFactory idGenerator;
    private final PasswordStrengthChecker passwordChecker;

    @Override
    public Mono<Entity<User>> get(String userId) {
        return userRepository.get(userId)
                .flatMap(this::filterPublicData);
    }

    @Override
    public Flux<Entity<User>> list(PageRequest pageRequest) {
        return authFacade.getConnectedUser()
                .map(u -> QueryContext.from(pageRequest).withUserId(u.id))
                .flatMapMany(userRepository::list)
                .flatMap(this::filterPublicData);
    }

    @Override
    public Mono<Integer> count(PageRequest pageRequest) {
        return authFacade.getConnectedUser()
                .map(u -> QueryContext.all(pageRequest.filter()).withUserId(u.id))
                .flatMap(userRepository::count);
    }

    @Override
    public Mono<Entity<User>> create(User user) {
        String userId = String.format("%s%s", ID_PREFIX, idGenerator.create());
        User withPassword = user.withPassword(passwordEncoder.encode(user.password));
        Entity<User> entity = Entity.identify(userId, clock.instant(), withPassword);
        return authorizeAllData()
                .flatMap(u -> userRepository.persist(List.of(entity)).single())
                .then(userRepository.persist(userId, user.roles.stream().map(Permission::toString).distinct().toList()));
    }

    @Override
    public Mono<Entity<User>> update(String id, User user) {
        return authorizeSelfData(id).flatMap(u -> {
            User checkedUser = user.toBuilder()
                    .password(Objects.nonNull(user.password) ? passwordEncoder.encode(user.password) : null)
                    .build();
            return userRepository.update(id, checkedUser);
        });
    }

    @Override
    public Mono<Void> changePassword(String id, String oldPassword, String newPassword) {
        return authorizeSelfData(id).mapNotNull(user -> {
            PasswordStrength strength = passwordChecker.estimate(newPassword, user.self, Locale.FRANCE);
            log.info("Change password for {}", newPassword);

            log.info("New password is safe : {}", strength.isSafe());
            log.info("New password entropie : {}", strength.entropy());
            log.info("Estimate : {}", strength.timeToCrack());

            return null;
        });
    }

    @Override
    public Flux<Entity<User>> delete(Collection<String> ids) {
        return authorizeSelfData(ids)
                .flatMapMany(u -> userRepository.list(QueryContext.all(Criteria.property(ID).in(ids))))
                .switchIfEmpty(Flux.error(new NoSuchElementException(String.format("Unable to find users %s :", ids))))
                .collectList()
                .flatMapMany(users -> Flux.fromIterable(users)
                        .map(u -> u.id)
                        .collectList()
                        .flatMap(userRepository::delete)
                        .thenMany(Flux.fromIterable(users)));
    }

    @Override
    public Mono<Entity<User>> grants(String grantedUserId, Collection<Permission> permissions) {
        return authFacade.getConnectedUser().flatMap(currentUser -> {
            var tobeVerified = new ArrayList<String>();
            boolean selfGrant = currentUser.id.equals(grantedUserId);

            for (Permission perm : permissions) {
                if (!RoleUtils.hasPermission(currentUser.self, perm)) {
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

        }).flatMap(currentUser -> userRepository.persist(grantedUserId,
                permissions.stream().map(Permission::toString).distinct().toList()));
    }

    @Override
    public Mono<Void> revokes(Permission permission, Collection<String> userIds) {
        return authFacade.getConnectedUser().flatMap(currentUser -> {
            if ((userIds.size() == 1 && userIds.contains(currentUser.id)) || RoleUtils.hasRole(currentUser.self, Role.ADMIN)) {
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
                .filter(u -> (hasRole(u.self, Role.ADMIN)
                        || (hasRole(u.self, Role.USER) && original.id.equals(u.id))))
                .map(u -> original)
                .switchIfEmpty(Mono.just(Entity.<User>builder()
                        .id(original.id)
                        .createdBy(Entity.NO_ONE)
                        .createdAt(Instant.EPOCH)
                        .self(original.self.toBuilder()
                                .clearRoles()
                                .password(null)
                                .mail(null)
                                .build())
                        .build()));
    }

    private Mono<Entity<User>> authorizeSelfData(String id) {
        return authorizeSelfData(Collections.singleton(id));
    }

    private Mono<Entity<User>> authorizeSelfData(Collection<String> ids) {
        return authFacade.getConnectedUser()
                .switchIfEmpty(Mono.error(new UnauthenticatedUser(AUTHENTICATION_NOT_FOUND)))
                .handle((u, sink) -> {
                    if (hasRole(u.self, Role.ADMIN)
                            || (hasRole(u.self, Role.USER) && ids.size() == 1 && ids.contains(u.id))) {
                        sink.next(u);
                        return;
                    }
                    sink.error(new UnauthorizedOperation("User unauthorized for the operation !"));
                });
    }

    private Mono<Entity<User>> authorizeAllData() {
        return authFacade.getConnectedUser()
                .switchIfEmpty(Mono.error(new UnauthenticatedUser(AUTHENTICATION_NOT_FOUND)))
                .handle((u, sink) -> {
                    if (hasRole(u.self, Role.ADMIN)) {
                        sink.next(u);
                        return;
                    }
                    sink.error(new UnauthorizedOperation("User unauthorized for the operation !"));
                });
    }
}
