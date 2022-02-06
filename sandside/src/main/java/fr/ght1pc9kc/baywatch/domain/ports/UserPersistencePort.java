package fr.ght1pc9kc.baywatch.domain.ports;

import fr.ght1pc9kc.baywatch.api.common.model.Entity;
import fr.ght1pc9kc.baywatch.api.security.model.User;
import fr.ght1pc9kc.baywatch.domain.techwatch.model.QueryContext;
import fr.ght1pc9kc.juery.api.PageRequest;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Collection;

public interface UserPersistencePort {
    Mono<Entity<User>> get(String id);

    Flux<Entity<User>> list(PageRequest pageRequest);

    Flux<Entity<User>> list();

    Mono<Integer> count(QueryContext qCtx);

    Flux<Entity<User>> persist(Collection<Entity<User>> users);

    Mono<Integer> delete(Collection<String> id);
}
