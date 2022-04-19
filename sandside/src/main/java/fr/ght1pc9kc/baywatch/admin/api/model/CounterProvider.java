package fr.ght1pc9kc.baywatch.admin.api.model;

import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.Instant;

public interface CounterProvider {
    CounterType name();

    default Mono<BigDecimal> computeNumeric() {
        return Mono.error(IllegalStateException::new);
    }

    default Mono<Instant> computeInstant() {
        return Mono.error(IllegalStateException::new);
    }

    default Mono<String> computeString() {
        return Mono.error(IllegalStateException::new);
    }
}
