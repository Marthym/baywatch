package fr.ght1pc9kc.baywatch.domain;

import fr.ght1pc9kc.baywatch.api.UserService;
import fr.ght1pc9kc.baywatch.api.model.User;
import fr.ght1pc9kc.baywatch.api.model.request.PageRequest;
import fr.ght1pc9kc.baywatch.domain.ports.UserPersistencePort;
import lombok.AllArgsConstructor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@AllArgsConstructor
public final class UserServiceImpl implements UserService {
    private final UserPersistencePort userRepository;

    @Override
    public Mono<User> get(String userId) {
        return userRepository.get(userId);
    }

    @Override
    public Flux<User> list(PageRequest pageRequest) {
        return userRepository.list(pageRequest);
    }
}
