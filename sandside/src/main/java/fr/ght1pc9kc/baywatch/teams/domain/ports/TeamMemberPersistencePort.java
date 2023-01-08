package fr.ght1pc9kc.baywatch.teams.domain.ports;

import fr.ght1pc9kc.baywatch.common.api.model.Entity;
import fr.ght1pc9kc.baywatch.teams.api.model.TeamMember;
import fr.ght1pc9kc.baywatch.teams.domain.model.PendingFor;
import fr.ght1pc9kc.baywatch.techwatch.domain.model.QueryContext;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Collection;

public interface TeamMemberPersistencePort {
    Flux<Entity<TeamMember>> list(QueryContext qCtx);

    Mono<Void> add(Collection<Entity<PendingFor>> requests);

    Mono<Void> remove(String teamId, Collection<String> membersIds);

    Mono<Void> clear(Collection<String> teamsIds);
}
