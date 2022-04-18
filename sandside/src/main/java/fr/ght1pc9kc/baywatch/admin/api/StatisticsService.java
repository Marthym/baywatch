package fr.ght1pc9kc.baywatch.admin.api;

import reactor.core.publisher.Mono;

public interface StatisticsService {
    Mono<Integer> getNewsCount();

    Mono<Integer> getFeedsCount();

    Mono<Integer> getUsersCount();
}
