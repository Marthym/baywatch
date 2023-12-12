package fr.ght1pc9kc.baywatch.notify.domain.ports;

import fr.ght1pc9kc.baywatch.notify.api.model.ServerEvent;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface NotificationPersistencePort {
    Mono<ServerEvent> persist(ServerEvent event);

    Flux<ServerEvent> consume(String userId);
}
