package fr.ght1pc9kc.baywatch.admin.domain.ports;

import fr.ght1pc9kc.baywatch.common.domain.QueryContext;
import reactor.core.publisher.Mono;

public interface RawNewsPersistencePort {
    Mono<Void> delete(QueryContext qCtx);
}
