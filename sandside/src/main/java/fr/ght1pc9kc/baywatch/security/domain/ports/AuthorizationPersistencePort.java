package fr.ght1pc9kc.baywatch.security.domain.ports;

import fr.ght1pc9kc.baywatch.security.api.model.Permission;
import fr.ght1pc9kc.baywatch.security.api.model.User;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Collection;
import java.util.Map.Entry;
import java.util.Set;

public interface AuthorizationPersistencePort {

    /**
     * Count the number of {@link User} having at leat one of the giving permissions
     *
     * @param permissions The list of permissions to count in database
     * @return The number of users
     */
    Mono<Integer> count(Collection<String> permissions);

    /**
     * List all {@link Permission}s for each {@link User} in the given collection
     *
     * @param userIds The users ids
     * @return The permissions for each user
     */
    Flux<Entry<String, Set<Permission>>> list(Collection<String> userIds);

    /**
     * List all {@link User}s IDs granted with the given {@link Permission}
     *
     * @param permission The permission
     * @return The users IDs
     */
    Flux<String> grantees(Permission permission);

    /**
     * Remove {@link Permission} list from the database
     *
     * @param permissions The permissions to remove
     * @return {@code Void} when operation complete
     */
    Mono<Void> remove(Collection<Permission> permissions);
}
