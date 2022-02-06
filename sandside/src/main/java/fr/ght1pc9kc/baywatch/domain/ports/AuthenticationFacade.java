package fr.ght1pc9kc.baywatch.domain.ports;

import fr.ght1pc9kc.baywatch.api.common.model.Entity;
import fr.ght1pc9kc.baywatch.api.security.model.User;
import reactor.core.publisher.Mono;
import reactor.util.context.Context;

/**
 * Facade used to obtain the connected user.
 */
public interface AuthenticationFacade {

    /**
     * Obtain the connected user.
     *
     * @return The connected user.
     */
    Mono<Entity<User>> getConnectedUser();

    /**
     * Update the {@link org.reactivestreams.Publisher} context with a specific authentication
     *
     * @param user The {@link User} as Principal of the Authentication
     * @return The authenticated Context
     */
    Context withAuthentication(Entity<User> user);

    Context withSystemAuthentication();
}
