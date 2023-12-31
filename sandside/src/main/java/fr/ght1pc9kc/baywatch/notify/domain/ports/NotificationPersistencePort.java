package fr.ght1pc9kc.baywatch.notify.domain.ports;

import fr.ght1pc9kc.baywatch.common.api.model.Entity;
import fr.ght1pc9kc.baywatch.notify.api.model.ServerEvent;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface NotificationPersistencePort {
    Mono<ServerEvent> persist(Entity<ServerEvent> event);

    Flux<ServerEvent> consume(String userId);
}
