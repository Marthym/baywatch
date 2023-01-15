package fr.ght1pc9kc.baywatch.teams.domain.ports;

import fr.ght1pc9kc.baywatch.common.api.model.Entity;
import fr.ght1pc9kc.baywatch.teams.api.model.Team;
import fr.ght1pc9kc.baywatch.techwatch.domain.model.QueryContext;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Collection;

public interface TeamPersistencePort {
    Flux<Entity<Team>> list(QueryContext qCtx);

    Mono<Integer> count(QueryContext qCtx);

    Mono<Void> persist(Entity<Team> toPersist);

    Mono<Void> delete(Collection<String> ids);
}
