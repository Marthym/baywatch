package fr.ght1pc9kc.baywatch.admin.api;

import fr.ght1pc9kc.baywatch.admin.api.model.Counter;
import fr.ght1pc9kc.baywatch.admin.api.model.CounterGroup;
import reactor.core.publisher.Flux;

public interface StatisticsService {
    Flux<Counter> compute(CounterGroup group);

    Flux<Counter> compute();
}
