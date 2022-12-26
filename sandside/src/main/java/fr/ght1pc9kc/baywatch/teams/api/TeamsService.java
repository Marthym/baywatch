package fr.ght1pc9kc.baywatch.teams.api;

import fr.ght1pc9kc.baywatch.common.api.model.Entity;
import fr.ght1pc9kc.baywatch.teams.api.model.Team;
import fr.ght1pc9kc.juery.api.PageRequest;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Collection;

/**
 * TeamService allow user to manage teams and team members
 * A {@link Team} is a set of {@link fr.ght1pc9kc.baywatch.security.api.model.User}
 * aka members who can share {@link fr.ght1pc9kc.baywatch.techwatch.api.model.News}.
 */
public interface TeamsService {
    /**
     * Get a single {@link Team} by its ID
     *
     * @param id An ULID identifiant for the Team
     * @return The team and its members IDs.
     */
    Mono<Entity<Team>> get(String id);

    /**
     * Create a new {@link Team} from a simple name. The created Team
     * has for unique member the creator.
     *
     * @param name The name of the team
     * @return The new created Team with its only member
     */
    Mono<Entity<Team>> create(String name);

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

    /**
     * Update a {@link Team} or the members of the team
     * Only {@code MANAGER}s of the given Team are authorized to update
     *
     * @param id     The ULID of the team to update
     * @param entity The new Team Entity
     * @return The updated Team with its members
     * @throws fr.ght1pc9kc.baywatch.teams.api.exceptions.TeamPermissionDenied if Team ULID is not managed by the
     *                                                                         current {@link fr.ght1pc9kc.baywatch.security.api.model.User}
     */
    Mono<Entity<Team>> update(String id, Team entity);

    /**
     * Allow a {@code MANAGER} to drop one or more {@link Team}s.
     * The connected user must have {@code MANAGER} role for all
     * the Teams passed in argument.
     *
     * @param ids A collection of {@link Team} ULIDs to delete
     * @return The deleted Teams ULIDs
     * @throws fr.ght1pc9kc.baywatch.teams.api.exceptions.TeamPermissionDenied when one or more ids was not managed by
     *                                                                         current {@link fr.ght1pc9kc.baywatch.security.api.model.User}
     */
    Flux<Entity<Team>> delete(Collection<String> ids);
}
