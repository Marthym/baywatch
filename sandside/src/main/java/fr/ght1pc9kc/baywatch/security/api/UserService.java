package fr.ght1pc9kc.baywatch.security.api;

import fr.ght1pc9kc.baywatch.common.api.model.Entity;
import fr.ght1pc9kc.baywatch.security.api.model.Permission;
import fr.ght1pc9kc.baywatch.security.api.model.Role;
import fr.ght1pc9kc.baywatch.security.api.model.User;
import fr.ght1pc9kc.juery.api.PageRequest;
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
     * Grant a set of {@link Permission} to a {@link User}.
     * A {@code Connected User} can grant permission only if he always have the permission himself
     * or if the {@link Permission} is an {@code Authorization} on entity and if no other user have the same permission.
     *
     * @param userId      The user ID to grant
     * @param permissions The set of permission to grant to the user
     * @return The granted user with the new set of permission.
     */
    Mono<Entity<User>> grants(String userId, Collection<Permission> permissions);

    /**
     * Revoke a set of {@link Permission} from a {@link User}.
     * An {@link Role#ADMIN} user can revoke any permissions to any user.
     * A standard user can only revoke its own permissions
     *
     * @param userId      The user ID to revoke
     * @param permissions The set of permission to revoke to the user
     * @return The revoked user with the new set of permission.
     */
    Mono<Entity<User>> revokes(String userId, Collection<Permission> permissions);
}
