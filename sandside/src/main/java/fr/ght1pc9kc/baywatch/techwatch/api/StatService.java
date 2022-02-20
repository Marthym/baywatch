package fr.ght1pc9kc.baywatch.techwatch.api;

import reactor.core.publisher.Mono;

public interface StatService {
    Mono<Integer> getNewsCount();

    Mono<Integer> getFeedsCount();

    Mono<Integer> getUnreadCount();
}
