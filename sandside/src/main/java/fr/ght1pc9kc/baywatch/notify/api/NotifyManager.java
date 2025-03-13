package fr.ght1pc9kc.baywatch.notify.api;

import org.springframework.http.codec.ServerSentEvent;
import reactor.core.publisher.FluxSink;

/**
 * The service to use for Notification Flux manipulation
 */
public interface NotifyManager extends NotifyService {
    /**
     * This allows Service or whatever to subscribe to notifications
     */
    void subscribe(FluxSink<ServerSentEvent<?>> sink);

    /**
     * Close the multicast Flux and dispose all subscriptions.
     */
    void close();
}
