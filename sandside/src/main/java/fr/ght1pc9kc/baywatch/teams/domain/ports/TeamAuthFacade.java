package fr.ght1pc9kc.baywatch.teams.domain.ports;

import fr.ght1pc9kc.baywatch.common.api.model.Entity;
import fr.ght1pc9kc.baywatch.security.api.model.User;
import reactor.core.publisher.Mono;

import java.util.Collection;

public interface TeamAuthFacade {
    /**
     * Obtain the connected user.
     *
     * @return The connected user.
     */
    Mono<Entity<User>> getConnectedUser();

    /**
     * Grant a {@code MANAGER} permission for et team to the current user
     *
     * @param authorizations The permissions to grant
     * @return {@code Void} when operation was finished
     * @throws IllegalArgumentException if authorization does not contain entity ID or if the role is unknown
     */
    Mono<Void> grantAuthorization(Collection<String> authorizations);

    /**
     * Revoke a {@code MANAGER} permission for et team to the current user
     *
     * @param authorizations The permissions to revoke
     * @return {@code Void} when operation was finished
     * @throws IllegalArgumentException if authorization does not contain entity ID or if the role is unknown
     */
    Mono<Void> revokeAuthorization(Collection<String> authorizations);
}
