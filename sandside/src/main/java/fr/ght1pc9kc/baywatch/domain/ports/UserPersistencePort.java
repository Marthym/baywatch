package fr.ght1pc9kc.baywatch.domain.ports;

import fr.ght1pc9kc.baywatch.api.common.model.Entity;
import fr.ght1pc9kc.baywatch.api.security.model.User;
import fr.ght1pc9kc.baywatch.domain.techwatch.model.QueryContext;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Collection;

public interface UserPersistencePort {
    Mono<Entity<User>> get(String id);

    Flux<Entity<User>> list(QueryContext qCtx);

    Flux<Entity<User>> list();

    Mono<Integer> count(QueryContext qCtx);

    /**
     * Persist all {@link Entity} given as argument.
     * If error throw during insert, all the transaction was rollback and nothinh was persisted
     *
     * @param users A collection af {@link User} {@link Entity}
     * @return the persisted {@link User} {@link Entity}
     */
    Flux<Entity<User>> persist(Collection<Entity<User>> users);

    Mono<Entity<User>> update(String id, User user);

    Mono<Integer> delete(Collection<String> id);
}
