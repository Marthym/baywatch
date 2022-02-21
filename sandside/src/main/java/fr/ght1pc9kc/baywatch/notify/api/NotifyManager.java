package fr.ght1pc9kc.baywatch.notify.api;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface NotifyManager {
    Flux<ServerEvent<Object>> subscribe();

    Mono<Boolean> unsubscribe();

    void close();
}
