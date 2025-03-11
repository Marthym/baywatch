package fr.ght1pc9kc.baywatch.notify.domain;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.f4b6a3.ulid.UlidFactory;
import fr.ght1pc9kc.baywatch.notify.api.NotifyManager;
import fr.ght1pc9kc.baywatch.notify.api.NotifyService;
import fr.ght1pc9kc.baywatch.notify.api.model.BasicEvent;
import fr.ght1pc9kc.baywatch.notify.api.model.EventType;
import fr.ght1pc9kc.baywatch.notify.api.model.ReactiveEvent;
import fr.ght1pc9kc.baywatch.notify.api.model.ServerEvent;
import fr.ght1pc9kc.baywatch.notify.domain.ports.NotificationPersistencePort;
import fr.ght1pc9kc.baywatch.security.api.AuthenticationFacade;
import fr.ght1pc9kc.entity.api.Entity;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.Nullable;
import org.springframework.http.codec.ServerSentEvent;
import reactor.core.Disposable;
import reactor.core.Scannable;
import reactor.core.publisher.FluxSink;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;
import reactor.core.publisher.Sinks.EmitResult;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

import java.time.Clock;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static fr.ght1pc9kc.baywatch.common.api.DefaultMeta.createdAt;
import static fr.ght1pc9kc.baywatch.common.api.DefaultMeta.createdBy;

@Slf4j
public class NotifyServiceImpl implements NotifyService, NotifyManager {
    private static final String PREFIX = "EV";

    private final AuthenticationFacade authFacade;
    private final NotificationPersistencePort notificationPersistence;

    private final Sinks.Many<ServerEvent> multicast;
    private final Cache<String, Tuple2<Sinks.Many<ServerEvent>, List<Disposable>>> cache;
    private final Clock clock;
    private final UlidFactory ulidFactory = UlidFactory.newMonotonicInstance();

    public NotifyServiceImpl(
            AuthenticationFacade authenticationFacade, NotificationPersistencePort notificationPersistence,
            Clock clock) {
        this.notificationPersistence = notificationPersistence;
        this.authFacade = authenticationFacade;
        this.multicast = Sinks.many().multicast().directBestEffort();
        this.clock = clock;
        this.cache = Caffeine.newBuilder()
                .expireAfterAccess(Duration.ofDays(1))
                .maximumSize(10_000)
                .<String, Tuple2<Sinks.Many<ServerEvent>, List<Disposable>>>removalListener((key, sink, cause) -> {
                    if (sink != null) {
                        log.atTrace().addArgument(key).log("Remove {} from the cache");
                        sink.getT1().tryEmitComplete();
                    }
                })
                .build();
    }

    @Override
    public void subscribe(FluxSink<ServerSentEvent<?>> sseSink) {
        if (multicast.isScanAvailable() && Boolean.TRUE.equals(multicast.scan(Scannable.Attr.TERMINATED))) {
            sseSink.error(new IllegalStateException("Publisher was closed !"));
        }
        Disposable mainDisposable = authFacade.getConnectedUser().flatMapMany(u -> {
                            var sinkAndDisposables = Objects.requireNonNull(cache.get(u.id(), id -> {
                                Sinks.Many<ServerEvent> sink = Sinks.many().multicast().onBackpressureBuffer();
                                List<Disposable> disposables = new ArrayList<>(2);
                                disposables.add(this.multicast.asFlux().subscribe(sink::tryEmitNext));
                                disposables.add(notificationPersistence.consume(u.id()).subscribe(sink::tryEmitNext));
                                return Tuples.of(sink, disposables);
                            }));
                            return sinkAndDisposables
                                    .getT1().asFlux()
                                    .doOnCancel(() -> {
                                        log.atDebug()
                                                .addArgument(u.id())
                                                .addArgument(sinkAndDisposables.getT1().currentSubscriberCount())
                                                .log("Cancel notification for {}, {}");
                                        if (sinkAndDisposables.getT1().currentSubscriberCount() <= 1) {
                                            sinkAndDisposables.getT2().forEach(Disposable::dispose);
                                            cache.invalidate(u.id());
                                        }
                                    })
                                    .doOnSubscribe(ignore -> log.atDebug()
                                            .addArgument(u.id())
                                            .log("Subscribe for {}"))
                                    .flatMap(evt -> switch (evt) {
                                        case BasicEvent<?> basic -> Mono.just(ServerSentEvent.builder()
                                                .id(basic.id())
                                                .event(basic.type().getName())
                                                .data(basic.message())
                                                .build());
                                        case ReactiveEvent<?> reactive -> reactive.message().map(msg -> ServerSentEvent.builder()
                                                .id(reactive.id())
                                                .event(reactive.type().getName())
                                                .data(msg)
                                                .build());
                                    });
                        }
                )
                .contextWrite(sseSink.contextView())
                .subscribe(sseSink::next);

        sseSink.onCancel(mainDisposable);
        sseSink.next(ServerSentEvent.builder()
                .id(ulidFactory.create().toString())
                .event(EventType.PING.getName())
                .build());
    }

    @Override
    public Mono<Boolean> unsubscribe() {
        return authFacade.getConnectedUser()
                .filter(u -> cache.asMap().containsKey(u.id()))
                .map(u -> {
                    log.atDebug().addArgument(u.id()).log("Dispose SSE Subscription for {}");
                    cache.invalidate(u.id());
                    return true;
                });
    }

    @Override
    public void close() {
        this.multicast.tryEmitComplete();
        this.cache.invalidateAll();
        this.cache.cleanUp();
        log.atWarn().addArgument(this.multicast.currentSubscriberCount())
                .log("Close multicast notifications channel ({} indisposed subscription(s))!");
    }

    @Override
    public <T> BasicEvent<T> send(String userId, EventType type, T data) {
        BasicEvent<T> event = new BasicEvent<>(PREFIX + ulidFactory.create().toString(), type, data);
        Optional.ofNullable(cache.getIfPresent(userId))
                .ifPresentOrElse(
                        sk -> emit(sk.getT1(), event),
                        () -> notificationPersistence.persist(Entity.identify(event)
                                .meta(createdBy, userId)
                                .meta(createdAt, clock.instant())
                                .withId(event.id())
                        ).subscribe());
        return event;
    }

    @Override
    public <T> ReactiveEvent<T> send(String userId, EventType type, Mono<T> data) {
        ReactiveEvent<T> event = new ReactiveEvent<>(PREFIX + ulidFactory.create().toString(), type, data);
        Optional.ofNullable(cache.getIfPresent(userId))
                .ifPresentOrElse(
                        sk -> emit(sk.getT1(), event),
                        () -> notificationPersistence.persist(Entity.identify(event)
                                .meta(createdBy, userId)
                                .meta(createdAt, clock.instant())
                                .withId(event.id())
                        ).subscribe());
        return event;
    }

    @Override
    public <T> BasicEvent<T> broadcast(EventType type, T data) {
        BasicEvent<T> event = new BasicEvent<>(PREFIX + ulidFactory.create().toString(), type, data);
        emit(this.multicast, event);
        return event;
    }

    @Override
    public <T> ReactiveEvent<T> broadcast(EventType type, Mono<T> data) {
        ReactiveEvent<T> event = new ReactiveEvent<>(PREFIX + ulidFactory.create().toString(), type, data);
        emit(this.multicast, event);
        return event;
    }

    public long countCacheEntries() {
        return cache.estimatedSize();
    }

    private void emit(@Nullable Sinks.Many<ServerEvent> sink, ServerEvent event) {
        if (sink == null) {
            log.atDebug().log("No subscriber listening the SSE entry point.");
            return;
        }
        EmitResult result = sink.tryEmitNext(event);
        if (result.isFailure()) {
            if (result == EmitResult.FAIL_ZERO_SUBSCRIBER) {
                log.atDebug().log("No subscriber listening the SSE entry point.");
            } else {
                log.atWarn().addArgument(result).log("{} on emit notification");
            }
        }
    }
}
