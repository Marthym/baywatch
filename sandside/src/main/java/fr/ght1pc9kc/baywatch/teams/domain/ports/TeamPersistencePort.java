package fr.ght1pc9kc.baywatch.teams.domain.ports;

import fr.ght1pc9kc.baywatch.common.api.model.Entity;
import fr.ght1pc9kc.baywatch.teams.api.model.Team;
import fr.ght1pc9kc.baywatch.techwatch.domain.model.QueryContext;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface TeamPersistencePort {
    Flux<Entity<Team>> list(QueryContext qCtx);

    Mono<Integer> count(QueryContext qCtx);

    Mono<Void> persist(String id, Team team);

    Mono<Void> delete(String id);
}
