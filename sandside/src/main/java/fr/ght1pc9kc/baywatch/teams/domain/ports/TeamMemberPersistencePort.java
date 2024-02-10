package fr.ght1pc9kc.baywatch.teams.domain.ports;

import fr.ght1pc9kc.baywatch.teams.api.model.TeamMember;
import fr.ght1pc9kc.baywatch.techwatch.domain.model.QueryContext;
import fr.ght1pc9kc.entity.api.Entity;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Collection;

public interface TeamMemberPersistencePort {
    Flux<Entity<TeamMember>> list(QueryContext qCtx);

    Mono<Void> add(Collection<Entity<TeamMember>> requests);

    Mono<Void> remove(String teamId, Collection<String> membersIds);

    /**
     * Delete all {@link TeamMember}s from the given {@link fr.ght1pc9kc.baywatch.teams.api.model.Team}s
     *
     * @param teamsIds The Teams IDs to clear
     * @return {@code void} when operation was complete
     */
    Mono<Void> clear(Collection<String> teamsIds);
}
