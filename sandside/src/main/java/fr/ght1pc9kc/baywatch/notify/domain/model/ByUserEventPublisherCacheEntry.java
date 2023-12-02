package fr.ght1pc9kc.baywatch.notify.domain.model;

import fr.ght1pc9kc.baywatch.notify.api.model.ServerEvent;
import org.reactivestreams.Subscription;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;

import java.util.concurrent.atomic.AtomicReference;

public record ByUserEventPublisherCacheEntry(
        AtomicReference<Subscription> subscription,
        Sinks.Many<ServerEvent> sink,
        Flux<ServerEvent> flux
) {
}
