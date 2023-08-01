package fr.ght1pc9kc.baywatch.security.api;

import fr.ght1pc9kc.baywatch.common.api.model.Entity;
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
     * Update {@link User} data. The {@link Role}s are replaced by those provided in input
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
     * Change password for the {@link User} id
     *
     * @param id          The user Id to change password
     * @param oldPassword The current password to change
     * @param newPassword The new password
     * @return {@code Void} when the password is changed
     * @throws IllegalArgumentException When password strength is not enough
     */
    Mono<Void> changePassword(String id, String oldPassword, String newPassword);

    /**
     * Delete {@link User}s
     *
     * @param ids {@link User}s ID to delete
     * @return The list of deleted {@link User}s as {@link Entity}
     */
    Flux<Entity<User>> delete(Collection<String> ids);
}
