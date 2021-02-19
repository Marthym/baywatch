package fr.ght1pc9kc.baywatch.api;

import reactor.core.publisher.Mono;

public interface StatService {
    Mono<Integer> getUsersCount();

    Mono<Integer> getNewsCount();

    Mono<Integer> getFeedsCount();
}
