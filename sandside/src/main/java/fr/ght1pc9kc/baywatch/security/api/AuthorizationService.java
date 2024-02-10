package fr.ght1pc9kc.baywatch.security.api;

import fr.ght1pc9kc.baywatch.security.api.model.Permission;
import fr.ght1pc9kc.baywatch.security.api.model.Role;
import fr.ght1pc9kc.baywatch.security.api.model.User;
import fr.ght1pc9kc.entity.api.Entity;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Collection;

public interface AuthorizationService {
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
     * @param userIds    The user IDs to revoke
     * @param permission The permission to revoke to the users
     * @return The revoked user with the new set of permission.
     */
    Mono<Void> revokes(Permission permission, Collection<String> userIds);

    /**
     * Remove the {@link Permission} from the system and revoke for all {@link User}s.
     *
     * @param permissions The list of permission to remove
     * @return {@code Void} when operation completed
     */
    Mono<Void> remove(Collection<Permission> permissions);

    /**
     * List all {@link User}s IDs granted with the given {@link Permission}.
     *
     * @param permission The permission to check
     * @return The users IDs
     */
    Flux<String> listGrantedUsers(Permission permission);
}
