package fr.ght1pc9kc.baywatch.scraper.domain.ports;

import fr.ght1pc9kc.baywatch.security.api.model.User;
import fr.ght1pc9kc.entity.api.Entity;
import reactor.core.publisher.Mono;
import reactor.util.context.Context;

public interface ScrapingAuthentFacade {
    /**
     * Obtain the connected user.
     *
     * @return The connected user.
     */
    Mono<Entity<User>> getConnectedUser();

    Context withSystemAuthentication();

    boolean hasSystemRole(Entity<User> current);
}
