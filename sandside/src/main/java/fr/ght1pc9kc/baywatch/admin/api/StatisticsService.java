package fr.ght1pc9kc.baywatch.admin.api;

import fr.ght1pc9kc.baywatch.admin.api.model.Counter;
import fr.ght1pc9kc.baywatch.admin.api.model.CounterType;
import reactor.core.publisher.Mono;

public interface StatisticsService {
    Mono<Counter> compute(CounterType type);
}
