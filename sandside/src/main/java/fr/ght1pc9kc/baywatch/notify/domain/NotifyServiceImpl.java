package fr.ght1pc9kc.baywatch.notify.domain;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
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

import java.time.Clock;
import java.time.Duration;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;

import static fr.ght1pc9kc.baywatch.common.api.DefaultMeta.createdAt;
import static fr.ght1pc9kc.baywatch.common.api.DefaultMeta.createdBy;

@Slf4j
public class NotifyServiceImpl implements NotifyService, NotifyManager {
    private final AuthenticationFacade authFacade;
    private final NotificationPersistencePort notificationPersistence;

    private final Sinks.Many<ServerEvent> multicast;
    private final Cache<String, Sinks.Many<ServerEvent>> cache;
    private final Supplier<String> eventIdGenerator;
    private final Clock clock;

    public NotifyServiceImpl(
            AuthenticationFacade authenticationFacade, NotificationPersistencePort notificationPersistence,
            Supplier<String> eventIdGenerator, Clock clock) {
        this.notificationPersistence = notificationPersistence;
        this.authFacade = authenticationFacade;
        this.multicast = Sinks.many().multicast().directBestEffort();
        this.eventIdGenerator = eventIdGenerator;
        this.clock = clock;
        this.cache = Caffeine.newBuilder()
                .expireAfterAccess(Duration.ofDays(1))
                .maximumSize(10_000)
                .<String, Sinks.Many<ServerEvent>>removalListener((key, sink, cause) -> {
                    if (sink != null) {
                        log.atTrace().addArgument(key).log("Remove {} from the cache");
                        if (sink.currentSubscriberCount() > 0) {
                            sink.tryEmitComplete();
                        }
                    }
                })
                .build();
    }

    @Override
    @SuppressWarnings("CallingSubscribeInNonBlockingScope")
    public void subscribe(FluxSink<ServerSentEvent<?>> sseSink) {
        if (multicast.isScanAvailable() && Boolean.TRUE.equals(multicast.scan(Scannable.Attr.TERMINATED))) {
            sseSink.error(new IllegalStateException("Publisher was closed !"));
        }
        Disposable mainDisposable = authFacade.getConnectedUser().flatMapMany(u -> {
                            var personalSink = Objects.requireNonNull(cache.get(u.id(), id ->
                                    Sinks.many().multicast().onBackpressureBuffer()));
                            Disposable disposableBroadcast = this.multicast.asFlux().subscribe(personalSink::tryEmitNext);
                            Disposable disposablePersistence = notificationPersistence.consume(u.id()).subscribe(personalSink::tryEmitNext);
                            return personalSink
                                    .asFlux()
                                    .doFinally(signal -> {
                                        log.atDebug()
                                                .addArgument(u.id())
                                                .addArgument(personalSink.currentSubscriberCount())
                                                .log("Cancel notification for {}, {}");
                                        if (personalSink.currentSubscriberCount() <= 0) {
                                            disposableBroadcast.dispose();
                                            disposablePersistence.dispose();
                                            cache.invalidate(u.id());
                                        }
                                    })
                                    .doOnSubscribe(ignore ->
                                            log.atDebug().addArgument(u.id()).log("Subscribe for {}"))

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
                .id(eventIdGenerator.get())
                .event(EventType.OPEN.getName())
                .build());
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
        BasicEvent<T> event = new BasicEvent<>(eventIdGenerator.get(), type, data);
        Optional.ofNullable(cache.getIfPresent(userId))
                .ifPresentOrElse(
                        sk -> emit(sk, event),
                        () -> notificationPersistence.persist(Entity.identify(event)
                                .meta(createdBy, userId)
                                .meta(createdAt, clock.instant())
                                .withId(event.id())
                        ).subscribe());
        return event;
    }

    @Override
    public <T> ReactiveEvent<T> send(String userId, EventType type, Mono<T> data) {
        ReactiveEvent<T> event = new ReactiveEvent<>(eventIdGenerator.get(), type, data);
        Optional.ofNullable(cache.getIfPresent(userId))
                .ifPresentOrElse(
                        sk -> emit(sk, event),
                        () -> notificationPersistence.persist(Entity.identify(event)
                                .meta(createdBy, userId)
                                .meta(createdAt, clock.instant())
                                .withId(event.id())
                        ).subscribe());
        return event;
    }

    @Override
    public <T> BasicEvent<T> broadcast(EventType type, T data) {
        BasicEvent<T> event = new BasicEvent<>(eventIdGenerator.get(), type, data);
        emit(this.multicast, event);
        return event;
    }

    @Override
    public <T> ReactiveEvent<T> broadcast(EventType type, Mono<T> data) {
        ReactiveEvent<T> event = new ReactiveEvent<>(eventIdGenerator.get(), type, data);
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
