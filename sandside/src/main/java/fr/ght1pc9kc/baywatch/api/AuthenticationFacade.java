package fr.ght1pc9kc.baywatch.api;

import fr.ght1pc9kc.baywatch.api.model.User;
import reactor.core.publisher.Mono;

/**
 * Facade used to obtain the connected user.
 */
public interface AuthenticationFacade {

    /**
     * Obtain the connected user.
     *
     * @return The connected user.
     */
    Mono<User> getConnectedUser();
}
