package fr.ght1pc9kc.baywatch.notify.api;

import fr.ght1pc9kc.baywatch.notify.api.model.BasicEvent;
import fr.ght1pc9kc.baywatch.notify.api.model.EventType;
import fr.ght1pc9kc.baywatch.notify.api.model.ReactiveEvent;
import reactor.core.publisher.Mono;

public interface NotifyService {
    <T> BasicEvent<T> send(EventType type, T data);

    <T> ReactiveEvent<T> send(EventType type, Mono<T> data);
}
