package fr.ght1pc9kc.baywatch.admin.domain.ports;

import fr.ght1pc9kc.baywatch.admin.api.model.RawFeed;
import fr.ght1pc9kc.baywatch.common.domain.QueryContext;
import fr.ght1pc9kc.entity.api.Entity;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Collection;

public interface RawFeedPersistencePort {
    Flux<Entity<RawFeed>> list(QueryContext qCtx);

    Flux<Entity<RawFeed>> update(Collection<Entity<RawFeed>> toUpdate);

    Flux<Entity<RawFeed>> persist(Collection<Entity<RawFeed>> toPersist);

    Mono<Void> delete(Collection<String> toDelete);
}
