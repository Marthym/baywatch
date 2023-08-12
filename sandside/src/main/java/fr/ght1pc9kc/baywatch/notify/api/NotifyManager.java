package fr.ght1pc9kc.baywatch.notify.api;

import fr.ght1pc9kc.baywatch.notify.api.model.ServerEvent;
import reactor.core.Disposable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * The service to use for Notification Flux manipulation
 */
public interface NotifyManager extends NotifyService {
    /**
     * This allows Service or whatever to subscribe to notifications
     *
     * @return The notification flux
     */
    Flux<ServerEvent> subscribe();

    /**
     * Allow unsubscribing to notifications flux. This will make a {@link Disposable#dispose()}
     * on the subscription.
     *
     * @return {@code true} if unsubscribe successfully.
     */
    Mono<Boolean> unsubscribe();

    /**
     * Close the multicast Flux and dispose all subscriptions.
     */
    void close();
}
