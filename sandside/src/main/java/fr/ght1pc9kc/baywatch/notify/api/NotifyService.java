package fr.ght1pc9kc.baywatch.notify.api;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;

public interface NotifyService {
    Flux<Tuple2<EventType, Mono<Object>>> getFlux();

    <T> void send(EventType type, Mono<T> data);

    void close();
}
