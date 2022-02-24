package fr.ght1pc9kc.baywatch.notify.domain;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.f4b6a3.ulid.UlidCreator;
import fr.ght1pc9kc.baywatch.notify.api.NotifyManager;
import fr.ght1pc9kc.baywatch.notify.api.NotifyService;
import fr.ght1pc9kc.baywatch.notify.api.model.BasicEvent;
import fr.ght1pc9kc.baywatch.notify.api.model.EventType;
import fr.ght1pc9kc.baywatch.notify.api.model.ReactiveEvent;
import fr.ght1pc9kc.baywatch.notify.api.model.ServerEvent;
import fr.ght1pc9kc.baywatch.security.api.AuthenticationFacade;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;
import reactor.core.publisher.Sinks.EmitResult;

import java.time.Duration;

@Slf4j
public class NotifyServiceImpl implements NotifyService, NotifyManager {
    private final AuthenticationFacade authFacade;

    private final Sinks.Many<ServerEvent<Object>> sink;
    private final Cache<String, Flux<ServerEvent<Object>>> cache;

    public NotifyServiceImpl(AuthenticationFacade authenticationFacade) {
        this.authFacade = authenticationFacade;
        this.sink = Sinks.many().multicast().directBestEffort();
        this.cache = Caffeine.newBuilder()
                .expireAfterAccess(Duration.ofMinutes(30))
                .maximumSize(1000)
                .build();
    }

    @Override
    @SuppressWarnings("ReactiveStreamsNullableInLambdaInTransform")
    public Flux<ServerEvent<Object>> subscribe() {
        return authFacade.getConnectedUser().flatMapMany(u -> cache.get(u.id, id ->
                this.sink.asFlux()
                        .takeWhile(e -> cache.asMap().containsKey(id))
                        .map(e -> {
                            log.debug("Event: {}", e);
                            return e;
                        }).cache(0)
        ));
    }

    @Override
    public Mono<Boolean> unsubscribe() {
        return authFacade.getConnectedUser()
                .filter(u -> cache.asMap().containsKey(u.id))
                .map(u -> {
                    log.debug("Dispose SSE Subscription for {}", u.id);
                    cache.invalidate(u.id);
                    return true;
                });
    }

    @Override
    public void close() {
        this.cache.invalidateAll();
        this.sink.tryEmitComplete();
        this.cache.cleanUp();
    }

    @Override
    public <T> void send(EventType type, T data) {
        ServerEvent<Object> event = new BasicEvent<>(UlidCreator.getMonotonicUlid().toString(), type, data);
        EmitResult result = this.sink.tryEmitNext(event);
        if (result.isFailure()) {
            if (result == EmitResult.FAIL_ZERO_SUBSCRIBER) {
                log.debug("No subscriber listening the SSE entry point.");
            } else {
                log.warn("{} on emit notification", result);
            }
        }
    }

    @Override
    public <T> void send(EventType type, Mono<T> data) {
        ServerEvent<Object> event = new ReactiveEvent<>(UlidCreator.getMonotonicUlid().toString(), type, data.map(Object.class::cast));
        EmitResult result = this.sink.tryEmitNext(event);
        if (result.isFailure()) {
            if (result == EmitResult.FAIL_ZERO_SUBSCRIBER) {
                log.debug("No subscriber listening the SSE entry point.");
            } else {
                log.warn("{} on emit notification", result);
            }
        }
    }
}
