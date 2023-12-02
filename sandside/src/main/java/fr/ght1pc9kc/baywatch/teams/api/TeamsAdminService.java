package fr.ght1pc9kc.baywatch.teams.api;

import fr.ght1pc9kc.baywatch.common.api.model.Entity;
import fr.ght1pc9kc.baywatch.teams.api.model.Team;
import fr.ght1pc9kc.juery.api.PageRequest;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface TeamsAdminService {
    /**
     * Get a single {@link Team} by its ID
     *
     * @param id An ULID identifiant for the Team
     * @return The team and its members IDs.
     */
    Mono<Entity<Team>> get(String id);

    /**
     * List all teh {@link Team} depending on the {@link PageRequest}
     * pass in parameter.
     *
     * @param pageRequest The filter and pagination information for the request
     * @return The resulting list with its members
     */
    Flux<Entity<Team>> list(PageRequest pageRequest);

    /**
     * Count the number of elements returned by the {@link PageRequest} parameters
     *
     * @param pageRequest The filter and pagination information for the request
     * @return The number of elements returned by the request
     */
    Mono<Integer> count(PageRequest pageRequest);
}
