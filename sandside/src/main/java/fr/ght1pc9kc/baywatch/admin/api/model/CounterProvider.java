package fr.ght1pc9kc.baywatch.admin.api.model;

import reactor.core.publisher.Mono;

public interface CounterProvider {
    CounterGroup group();

    default Mono<Counter> computeCounter() {
        return Mono.error(IllegalStateException::new);
    }
}
