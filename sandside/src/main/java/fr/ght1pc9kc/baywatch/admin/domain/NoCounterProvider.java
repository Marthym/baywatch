package fr.ght1pc9kc.baywatch.admin.domain;

import fr.ght1pc9kc.baywatch.admin.api.model.Counter;
import fr.ght1pc9kc.baywatch.admin.api.model.CounterProvider;
import fr.ght1pc9kc.baywatch.admin.api.model.CounterType;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import reactor.core.publisher.Mono;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class NoCounterProvider implements CounterProvider {
    public static final CounterProvider NOP = new NoCounterProvider();

    @Override
    public CounterType name() {
        return CounterType.NOP;
    }

    @Override
    public Mono<Counter> computeCounter() {
        return Mono.empty();
    }
}
