package fr.ght1pc9kc.baywatch.teams.domain.ports;

import fr.ght1pc9kc.baywatch.security.api.model.Role;
import fr.ght1pc9kc.baywatch.techwatch.domain.model.QueryContext;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Collection;
import java.util.Map.Entry;

public interface TeamMemberPersistencePort {
    Flux<Entry<String, String>> list(QueryContext qCtx);

    Mono<Void> add(String teamId, Role pendingFor, Collection<String> usersIds);

    Mono<Void> remove(String teamId, Collection<String> membersIds);

    Mono<Void> clear(Collection<String> teamsIds);
}
