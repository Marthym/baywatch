package fr.ght1pc9kc.baywatch.security.api;

import fr.ght1pc9kc.baywatch.common.api.model.Entity;
import fr.ght1pc9kc.baywatch.security.api.model.Role;
import fr.ght1pc9kc.baywatch.security.api.model.User;
import fr.ght1pc9kc.juery.api.PageRequest;
import org.jetbrains.annotations.Nullable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Collection;

public interface UserService {
    Mono<Entity<User>> get(String userId);

    Flux<Entity<User>> list(PageRequest pageRequest);

    /**
     * Count the total number of elements returned by the {@link PageRequest}
     * ignoring {@link fr.ght1pc9kc.juery.api.Pagination}
     *
     * @param pageRequest The page request with filters
     * @return The total elements count
     */
    Mono<Integer> count(PageRequest pageRequest);

    /**
     * Create new {@link User}
     *
     * @param user The user data to create
     * @return The new {@link User} {@link Entity} created. With ID and Created Date.
     */
    Mono<Entity<User>> create(User user);

    /**
     * Update {@link User} data. The {@link Role} was not updated.
     * <p>
     * The current user must have {@link Role#ADMIN} or more to update a user.
     * The user himself can update its data.
     *
     * @param id   The ID of the {@link User} {@link Entity} to be updated
     * @param user The new data for the {@link User}
     * @return The updates {@link User} {@link Entity}
     */
    Mono<Entity<User>> update(String id, User user);

    /**
     * Delete {@link User}s
     *
     * @param ids {@link User}s ID to delete
     * @return The list of deleted {@link User}s as {@link Entity}
     */
    Flux<Entity<User>> delete(Collection<String> ids);

    /**
     * Grant {@link User} to a new {@link Role}
     *
     * @param id   The User ID
     * @param role The role to grant the user
     * @return The user with the current updated roles
     */
    default Mono<Entity<User>> grantRole(String id, Role role) {
        return grantRole(id, role, null);
    }

    /**
     * Grant {@link User} to a new {@link Role}
     *
     * @param id     The User ID
     * @param role   The role to grant the user
     * @param entity The entity ID if the role is limited to an entity
     * @return The user with the current updated roles
     */
    Mono<Entity<User>> grantRole(String id, Role role, @Nullable String entity);

    /**
     * Revoke a {@link Role} accès for a {@link User}
     *
     * @param id   The User ID to revoke accès
     * @param role The revoked role
     * @return The User with the new updated role set
     */
    default Mono<Entity<User>> revokeRole(String id, Role role) {
        return revokeRole(id, role, null);
    }

    /**
     * Revoke a {@link Role} accès for a {@link User}
     *
     * @param id     The User ID to revoke accès
     * @param role   The revoked role
     * @param entity The entity ID to revoke the role limited to
     * @return The User with the new updated role set
     */
    Mono<Entity<User>> revokeRole(String id, Role role, @Nullable String entity);
}
