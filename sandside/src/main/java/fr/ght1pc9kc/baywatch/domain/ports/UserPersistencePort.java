package fr.ght1pc9kc.baywatch.domain.ports;

import fr.ght1pc9kc.baywatch.api.security.model.User;
import fr.ght1pc9kc.juery.api.PageRequest;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Collection;

public interface UserPersistencePort {
    Mono<User> get(String id);

    Flux<User> list(PageRequest pageRequest);

    Flux<User> list();

    Flux<User> persist(Collection<User> users);

    Mono<Integer> delete(Collection<String> id);
}
