package fr.ght1pc9kc.baywatch.api.techwatch;

import reactor.core.publisher.Mono;

public interface StatService {
    Mono<Integer> getNewsCount();

    Mono<Integer> getFeedsCount();

    Mono<Integer> getUnreadCount();
}
