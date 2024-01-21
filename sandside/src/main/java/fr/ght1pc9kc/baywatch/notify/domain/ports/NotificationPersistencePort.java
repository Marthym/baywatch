package fr.ght1pc9kc.baywatch.notify.domain.ports;

import fr.ght1pc9kc.baywatch.notify.api.model.ServerEvent;
import fr.ght1pc9kc.entity.api.Entity;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface NotificationPersistencePort {
    Mono<ServerEvent> persist(Entity<? extends ServerEvent> event);

    Flux<ServerEvent> consume(String userId);
}
