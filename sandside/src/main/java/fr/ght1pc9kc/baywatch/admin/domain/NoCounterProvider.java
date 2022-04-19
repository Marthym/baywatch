package fr.ght1pc9kc.baywatch.admin.domain;

import fr.ght1pc9kc.baywatch.admin.api.model.CounterProvider;
import fr.ght1pc9kc.baywatch.admin.api.model.CounterType;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.Instant;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class NoCounterProvider implements CounterProvider {
    public static final CounterProvider NOP = new NoCounterProvider();

    @Override
    public CounterType name() {
        return CounterType.NOP;
    }

    @Override
    public Mono<BigDecimal> computeNumeric() {
        return Mono.empty();
    }

    @Override
    public Mono<Instant> computeInstant() {
        return Mono.empty();
    }

    @Override
    public Mono<String> computeString() {
        return Mono.empty();
    }
}
