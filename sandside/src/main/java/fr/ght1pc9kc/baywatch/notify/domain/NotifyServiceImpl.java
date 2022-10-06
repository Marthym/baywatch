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
import fr.ght1pc9kc.baywatch.notify.domain.model.ByUserEventPublisherCacheEntry;
import fr.ght1pc9kc.baywatch.security.api.AuthenticationFacade;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.Nullable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;
import reactor.core.publisher.Sinks.EmitResult;

import java.time.Duration;
import java.util.Objects;

@Slf4j
public class NotifyServiceImpl implements NotifyService, NotifyManager {
    private final AuthenticationFacade authFacade;

    private final Sinks.Many<ServerEvent<Object>> multicast;
    private final Cache<String, ByUserEventPublisherCacheEntry> cache;

    public NotifyServiceImpl(AuthenticationFacade authenticationFacade) {
        this.authFacade = authenticationFacade;
        this.multicast = Sinks.many().multicast().directBestEffort();
        this.cache = Caffeine.newBuilder()
                .expireAfterAccess(Duration.ofMinutes(30))
                .maximumSize(1000)
                .<String, ByUserEventPublisherCacheEntry>removalListener((key, value, cause) -> {
                    if (value != null) {
                        value.sink().tryEmitComplete();
                    }
                })
                .build();
    }

    @Override
    public Flux<ServerEvent<Object>> subscribe() {
        return authFacade.getConnectedUser().flatMapMany(u ->
                Objects.requireNonNull(cache.get(u.id, id -> {
                    Sinks.Many<ServerEvent<Object>> sink = Sinks.many().multicast().directBestEffort();
                    Flux<ServerEvent<Object>> eventPublisher = Flux.merge(sink.asFlux(), this.multicast.asFlux())
                            .takeWhile(e -> cache.asMap().containsKey(id))
                            .map(e -> {
                                log.debug("Event: {}", e);
                                return e;
                            }).cache(0);
                    return new ByUserEventPublisherCacheEntry(sink, eventPublisher);
                })).flux());
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
        this.multicast.tryEmitComplete();
        this.cache.cleanUp();
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> BasicEvent<T> send(String userId, EventType type, T data) {
        BasicEvent<T> event = new BasicEvent<>(UlidCreator.getMonotonicUlid().toString(), type, data);
        Sinks.Many<ServerEvent<Object>> sink = Objects.requireNonNull(cache.getIfPresent(userId)).sink();
        emit(sink, (ServerEvent<Object>) event);
        return event;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> BasicEvent<T> broadcast(EventType type, T data) {
        BasicEvent<T> event = new BasicEvent<>(UlidCreator.getMonotonicUlid().toString(), type, data);
        emit(this.multicast, (ServerEvent<Object>) event);
        return event;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> ReactiveEvent<T> broadcast(EventType type, Mono<T> data) {
        ReactiveEvent<T> event = new ReactiveEvent<>(UlidCreator.getMonotonicUlid().toString(), type, data);
        emit(this.multicast, (ServerEvent<Object>) event);
        return event;
    }

    public long countCacheEntries() {
        return cache.estimatedSize();
    }

    private void emit(@Nullable Sinks.Many<ServerEvent<Object>> sink, ServerEvent<Object> event) {
        if (sink == null) {
            log.debug("No subscriber listening the SSE entry point.");
            return;
        }
        EmitResult result = sink.tryEmitNext(event);
        if (result.isFailure()) {
            if (result == EmitResult.FAIL_ZERO_SUBSCRIBER) {
                log.debug("No subscriber listening the SSE entry point.");
            } else {
                log.warn("{} on emit notification", result);
            }
        }
    }
}
