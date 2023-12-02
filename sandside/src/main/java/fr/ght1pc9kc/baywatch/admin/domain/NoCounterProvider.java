package fr.ght1pc9kc.baywatch.admin.domain;

import fr.ght1pc9kc.baywatch.admin.api.model.Counter;
import fr.ght1pc9kc.baywatch.admin.api.model.CounterGroup;
import fr.ght1pc9kc.baywatch.admin.api.model.CounterProvider;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import reactor.core.publisher.Mono;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class NoCounterProvider implements CounterProvider {
    public static final CounterProvider NOP = new NoCounterProvider();

    @Override
    public CounterGroup group() {
        return CounterGroup.SYSTEM;
    }

    @Override
    public Mono<Counter> computeCounter() {
        return Mono.empty();
    }
}
