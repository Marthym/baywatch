package fr.ght1pc9kc.baywatch.domain.security;

import fr.ght1pc9kc.baywatch.api.security.UserService;
import fr.ght1pc9kc.baywatch.api.security.model.User;
import fr.ght1pc9kc.baywatch.domain.exceptions.UnauthenticatedUser;
import fr.ght1pc9kc.baywatch.domain.ports.AuthenticationFacade;
import fr.ght1pc9kc.baywatch.domain.ports.UserPersistencePort;
import fr.ght1pc9kc.baywatch.domain.techwatch.model.QueryContext;
import fr.ght1pc9kc.juery.api.PageRequest;
import lombok.AllArgsConstructor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@AllArgsConstructor
public final class UserServiceImpl implements UserService {
    private final UserPersistencePort userRepository;
    private final AuthenticationFacade authFacade;

    @Override
    public Mono<User> get(String userId) {
        return userRepository.get(userId);
    }

    @Override
    public Flux<User> list(PageRequest pageRequest) {
        return userRepository.list(pageRequest);
    }

    @Override
    public Mono<Integer> count(PageRequest pageRequest) {
        return authFacade.getConnectedUser()
                .switchIfEmpty(Mono.error(new UnauthenticatedUser("Authentication not found !")))
                .map(u -> QueryContext.all(pageRequest.filter()).withUserId(u.id))
                .onErrorResume(UnauthenticatedUser.class, (e) -> Mono.just(QueryContext.all(pageRequest.filter())))
                .flatMap(userRepository::count);
    }
}
