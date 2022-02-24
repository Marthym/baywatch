package fr.ght1pc9kc.baywatch.notify.api;

import fr.ght1pc9kc.baywatch.notify.api.model.EventType;
import reactor.core.publisher.Mono;

public interface NotifyService {
    <T> void send(EventType type, T data);

    <T> void send(EventType type, Mono<T> data);
}
