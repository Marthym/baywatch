package fr.ght1pc9kc.baywatch.notify.api;

import fr.ght1pc9kc.baywatch.notify.api.model.BasicEvent;
import fr.ght1pc9kc.baywatch.notify.api.model.EventType;
import fr.ght1pc9kc.baywatch.notify.api.model.ReactiveEvent;
import reactor.core.publisher.Mono;

/**
 * The Service to use to push notifications
 */
public interface NotifyService {
    /**
     * Push notification into the multicasted Flux to all the subscribers
     *
     * @param type The type of the notification event
     * @param data The payload of the event
     * @param <T>  The Class of the payload
     * @return The final pushed event with ID
     */
    <T> BasicEvent<T> broadcast(EventType type, T data);

    <T> BasicEvent<T> send(String userId, EventType type, T data);

    /**
     * Push notification with Reactive payload, into the multicasted Flux to all the subscribers
     *
     * @param type The type of the notification event
     * @param data The reactive payload of the event
     * @param <T>  The Class of the payload
     * @return The final pushed event with ID
     */
    <T> ReactiveEvent<T> broadcast(EventType type, Mono<T> data);

    <T> ReactiveEvent<T> send(String userId, EventType type, Mono<T> data);
}
