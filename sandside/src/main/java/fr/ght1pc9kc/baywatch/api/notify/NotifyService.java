package fr.ght1pc9kc.baywatch.api.notify;

import reactor.core.publisher.Flux;
import reactor.util.function.Tuple2;

public interface NotifyService {
    Flux<Tuple2<EventType, Object>> getFlux();

    <T> void send(EventType type, T data);
}
