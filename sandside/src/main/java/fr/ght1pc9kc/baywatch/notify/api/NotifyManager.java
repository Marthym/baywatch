package fr.ght1pc9kc.baywatch.notify.api;

import org.springframework.http.codec.ServerSentEvent;
import reactor.core.Disposable;
import reactor.core.publisher.FluxSink;
import reactor.core.publisher.Mono;

/**
 * The service to use for Notification Flux manipulation
 */
public interface NotifyManager extends NotifyService {
    /**
     * This allows Service or whatever to subscribe to notifications
     */
    void subscribe(FluxSink<ServerSentEvent<?>> sink);

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
