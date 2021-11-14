package fr.ght1pc9kc.baywatch.api.notify;

import reactor.core.publisher.Flux;

public interface NotifyService {
    Flux<?> getFlux();

    <T> void send(T data);
}
