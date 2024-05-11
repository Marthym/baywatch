package fr.ght1pc9kc.baywatch.security.api;

import fr.ght1pc9kc.baywatch.security.api.model.Role;
import fr.ght1pc9kc.baywatch.security.api.model.User;
import fr.ght1pc9kc.entity.api.Entity;
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
     * <p>Create new {@link User}</p>
     * <p>Only {@link Role#ADMIN} and {@link User#ANONYMOUS} users can create new user. Regular Users have no permission
     * for that.</p>
     *
     * @param user The user data to create
     * @return The new {@link User} {@link Entity} created. With ID and Created Date.
     */
    Mono<Entity<User>> create(User user);

    /**
     * Update {@link User} data. The {@link Role}s are replaced by those provided in input
     * <p>
     * The current user must have {@link Role#ADMIN} or more to update a user.
     * The user himself can update its data.
     *
     * @param user The new data for the {@link User} as an {@link Entity} containing the ID and other metas of the User.
     * @return The updates {@link User} {@link Entity}
     */
    Mono<Entity<User>> update(Entity<User> user);

    /**
     * Update {@link User} data. The {@link Role}s are replaced by those provided in input
     * <p>
     * The current user must have {@link Role#ADMIN} or more to update a user.
     * The user himself can update its data.
     *
     * @param user            The new data for the {@link User} as an {@link Entity} containing the ID
     *                        and other metas of the User.
     * @param currentPassword The current password of the user who made the operation
     * @return The updates {@link User} {@link Entity}
     */
    Mono<Entity<User>> update(Entity<User> user, @Nullable String currentPassword);

    /**
     * Delete {@link User}s
     *
     * @param ids {@link User}s ID to delete
     * @return The list of deleted {@link User}s as {@link Entity}
     */
    Flux<Entity<User>> delete(Collection<String> ids);
}
