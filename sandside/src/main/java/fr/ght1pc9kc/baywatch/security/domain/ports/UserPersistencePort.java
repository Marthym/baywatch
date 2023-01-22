package fr.ght1pc9kc.baywatch.security.domain.ports;

import fr.ght1pc9kc.baywatch.common.api.model.Entity;
import fr.ght1pc9kc.baywatch.security.api.model.User;
import fr.ght1pc9kc.baywatch.techwatch.domain.model.QueryContext;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Collection;

public interface UserPersistencePort {
    Mono<Entity<User>> get(String id);

    Flux<Entity<User>> list(QueryContext qCtx);

    Flux<Entity<User>> list();

    Mono<Integer> count(QueryContext qCtx);

    /**
     * Count the number of {@link User} having at leat one of the giving permissions
     *
     * @param permissions The list of permissions to count in database
     * @return The number of users
     */
    Mono<Integer> countPermission(Collection<String> permissions);

    /**
     * Persist all {@link Entity} given as argument.
     * If error throw during insert, all the transaction was rollback and nothing was persisted
     *
     * @param users A collection af {@link User} {@link Entity}
     * @return the persisted {@link User} {@link Entity}
     */
    Flux<Entity<User>> persist(Collection<Entity<User>> users);

    /**
     * Persist new roles line for user id in roles tables
     *
     * @param userId The user ID
     * @param roles  The role string representation
     * @return The new list of roles for the user ID
     */
    Mono<Entity<User>> persist(String userId, Collection<String> roles);

    /**
     * Delete roles line for user id in roles tables
     *
     * @param userId The user ID
     * @param roles  The role string representation
     * @return The new list of roles for the user ID
     */
    Mono<Entity<User>> delete(String userId, Collection<String> roles);

    Mono<Entity<User>> update(String id, User user);

    Mono<Integer> delete(Collection<String> id);
}
