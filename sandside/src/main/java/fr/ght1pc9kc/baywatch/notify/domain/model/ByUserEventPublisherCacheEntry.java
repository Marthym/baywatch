package fr.ght1pc9kc.baywatch.notify.domain.model;

import fr.ght1pc9kc.baywatch.notify.api.model.ServerEvent;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;

public record ByUserEventPublisherCacheEntry(
        Sinks.Many<ServerEvent<Object>> sink,
        Flux<ServerEvent<Object>> flux
) {
}
