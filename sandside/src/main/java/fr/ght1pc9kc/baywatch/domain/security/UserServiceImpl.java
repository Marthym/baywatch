package fr.ght1pc9kc.baywatch.domain.security;

import fr.ght1pc9kc.baywatch.api.common.model.Entity;
import fr.ght1pc9kc.baywatch.api.security.UserService;
import fr.ght1pc9kc.baywatch.api.security.model.Role;
import fr.ght1pc9kc.baywatch.api.security.model.User;
import fr.ght1pc9kc.baywatch.domain.exceptions.UnauthenticatedUser;
import fr.ght1pc9kc.baywatch.domain.exceptions.UnauthorizedOperation;
import fr.ght1pc9kc.baywatch.domain.ports.AuthenticationFacade;
import fr.ght1pc9kc.baywatch.domain.ports.UserPersistencePort;
import fr.ght1pc9kc.baywatch.domain.techwatch.model.QueryContext;
import fr.ght1pc9kc.baywatch.domain.utils.Hasher;
import fr.ght1pc9kc.baywatch.domain.utils.MailAddress;
import fr.ght1pc9kc.juery.api.PageRequest;
import lombok.AllArgsConstructor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Clock;
import java.util.List;

@AllArgsConstructor
public final class UserServiceImpl implements UserService {
    private final UserPersistencePort userRepository;
    private final AuthenticationFacade authFacade;
    private final Clock clock;

    @Override
    public Mono<Entity<User>> get(String userId) {
        return handleAuthentication()
                .flatMap(u -> userRepository.get(userId));
    }

    @Override
    public Flux<Entity<User>> list(PageRequest pageRequest) {
        return handleAuthentication()
                .map(u -> QueryContext.all(pageRequest.filter()).withUserId(u.id))
                .flatMapMany(userRepository::list);
    }

    @Override
    public Mono<Integer> count(PageRequest pageRequest) {
        return handleAuthentication()
                .map(u -> QueryContext.all(pageRequest.filter()).withUserId(u.id))
                .flatMap(userRepository::count);
    }

    @Override
    public Mono<Entity<User>> create(User user) {
        String userMail = MailAddress.sanitize(user.mail);
        String userId = Hasher.sha3(userMail);
        Entity<User> entity = Entity.identify(userId, clock.instant(), user);
        return handleAuthentication()
                .flatMap(u -> userRepository.persist(List.of(entity)).single());
    }

    private Mono<Entity<User>> handleAuthentication() {
        return authFacade.getConnectedUser()
                .switchIfEmpty(Mono.error(new UnauthenticatedUser("Authentication not found !")))
                .map(u -> {
                    if (hasRole(u.entity, Role.ADMIN)) {
                        return u;
                    }
                    throw new UnauthorizedOperation("User unauthorized for the operation !");
                });
    }

    private static boolean hasRole(User user, Role expected) {
        return user.role.ordinal() <= expected.ordinal();
    }
}
