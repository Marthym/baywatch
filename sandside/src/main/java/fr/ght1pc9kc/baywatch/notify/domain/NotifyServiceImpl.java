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
import org.reactivestreams.Subscription;
import reactor.core.Scannable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;
import reactor.core.publisher.Sinks.EmitResult;

import java.time.Duration;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

@Slf4j
public class NotifyServiceImpl implements NotifyService, NotifyManager {
    private final AuthenticationFacade authFacade;

    private final Sinks.Many<ServerEvent> multicast;
    private final Cache<String, ByUserEventPublisherCacheEntry> cache;

    public NotifyServiceImpl(AuthenticationFacade authenticationFacade) {
        this.authFacade = authenticationFacade;
        this.multicast = Sinks.many().multicast().directBestEffort();
        this.cache = Caffeine.newBuilder()
                .expireAfterAccess(Duration.ofMinutes(30))
                .maximumSize(1000)
                .<String, ByUserEventPublisherCacheEntry>evictionListener((key, value, cause) -> {
                    if (value != null) {
                        value.sink().tryEmitComplete();
                        Subscription subscription = value.subscription().getAndSet(null);
                        if (subscription != null) {
                            subscription.cancel();
                        }
                    }
                })
                .build();
    }

    @Override
    public Flux<ServerEvent> subscribe() {
        if (multicast.isScanAvailable() && Boolean.TRUE.equals(multicast.scan(Scannable.Attr.TERMINATED))) {
            return Flux.error(() -> new IllegalStateException("Publisher was closed !"));
        }
        return authFacade.getConnectedUser().flatMapMany(u ->
                Objects.requireNonNull(cache.get(u.id, id -> {
                    Sinks.Many<ServerEvent> sink = Sinks.many().multicast().directBestEffort();
                    AtomicReference<Subscription> subscription = new AtomicReference<>();
                    Flux<ServerEvent> multicastFlux = this.multicast.asFlux().doOnSubscribe(subscription::set);
                    Flux<ServerEvent> eventPublisher = Flux.merge(sink.asFlux(), multicastFlux)
                            .takeWhile(e -> cache.asMap().containsKey(id))
                            .map(e -> {
                                log.debug("Event: {}", e);
                                return e;
                            }).cache(0);
                    return new ByUserEventPublisherCacheEntry(subscription, sink, eventPublisher);
                })).flux());
    }

    @Override
    public Mono<Boolean> unsubscribe() {
        return authFacade.getConnectedUser()
                .filter(u -> cache.asMap().containsKey(u.id))
                .map(u -> {
                    log.debug("Dispose SSE Subscription for {}", u.self.login);
                    cache.invalidate(u.id);
                    return true;
                });
    }

    @Override
    public void close() {
        this.multicast.tryEmitComplete();
        this.cache.invalidateAll();
        this.cache.cleanUp();
    }

    @Override
    public <T> BasicEvent<T> send(String userId, EventType type, T data) {
        BasicEvent<T> event = new BasicEvent<>(UlidCreator.getMonotonicUlid().toString(), type, data);
        Optional.ofNullable(cache.getIfPresent(userId))
                .map(ByUserEventPublisherCacheEntry::sink)
                .ifPresent(sk -> emit(sk, event));
        return event;
    }

    @Override
    public <T> ReactiveEvent<T> send(String userId, EventType type, Mono<T> data) {
        ReactiveEvent<T> event = new ReactiveEvent<>(UlidCreator.getMonotonicUlid().toString(), type, data);
        Optional.ofNullable(cache.getIfPresent(userId))
                .map(ByUserEventPublisherCacheEntry::sink)
                .ifPresent(sk -> emit(sk, event));
        return event;
    }

    @Override
    public <T> BasicEvent<T> broadcast(EventType type, T data) {
        BasicEvent<T> event = new BasicEvent<>(UlidCreator.getMonotonicUlid().toString(), type, data);
        emit(this.multicast, event);
        return event;
    }

    @Override
    public <T> ReactiveEvent<T> broadcast(EventType type, Mono<T> data) {
        ReactiveEvent<T> event = new ReactiveEvent<>(UlidCreator.getMonotonicUlid().toString(), type, data);
        emit(this.multicast, event);
        return event;
    }

    public long countCacheEntries() {
        return cache.estimatedSize();
    }

    private void emit(@Nullable Sinks.Many<ServerEvent> sink, ServerEvent event) {
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
