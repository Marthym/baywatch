package fr.ght1pc9kc.baywatch.notify.infra.controllers;

import com.github.f4b6a3.ulid.UlidFactory;
import fr.ght1pc9kc.baywatch.security.api.model.User;
import fr.ght1pc9kc.entity.api.Entity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.Nullable;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.Disposable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static java.util.Objects.nonNull;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/sse")
public class SseController {
    private final UlidFactory ulid = UlidFactory.newMonotonicInstance();
    private final Sinks.Many<ServerSentEvent<String>> notificationSink = Sinks.many().multicast().directBestEffort();
    private final Map<String, Sinks.Many<ServerSentEvent<String>>> byUserNotifications = new ConcurrentHashMap<>();

    @SuppressWarnings({"unchecked", "CallingSubscribeInNonBlockingScope"})
    @GetMapping(value = "/subscribe", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ServerSentEvent<String>> sse() {
        return ReactiveSecurityContextHolder.getContext()
                .map(ctx -> ((Entity<User>) ctx.getAuthentication().getPrincipal()).self().login())
                .flatMapMany(user -> Flux.create(sink -> {
                    var currentUserSink = byUserNotifications.computeIfAbsent(user, key -> Sinks.many().multicast().directBestEffort());
                    Disposable disposable = notificationSink.asFlux().subscribe(sink::next);
                    Disposable userDisposable = currentUserSink.asFlux().subscribe(sink::next);
                    sink.onCancel(() -> {
                        disposable.dispose();
                        userDisposable.dispose();
                        var userSink = byUserNotifications.get(user);
                        if (nonNull(userSink) && userSink.currentSubscriberCount() <= 0) {
                            log.info("User {} has {} subscribers", user, userSink.currentSubscriberCount());
                            byUserNotifications.remove(user);
                        }
                    });
                    sink.next(ServerSentEvent.<String>builder()
                            .id(ulid.create().toString())
                            .event("open")
                            .build());
                }));

    }

    @GetMapping("/send")
    public Mono<Void> send(@RequestParam("msg") String msg, @Nullable @RequestParam(value = "user", required = false) String user) {
        return Mono.fromCallable(() -> {
            if (nonNull(user)) {
                var userSink = byUserNotifications.get(user);
                if (nonNull(userSink)) {
                    return userSink.tryEmitNext(ServerSentEvent.<String>builder()
                            .id(ulid.create().toString())
                            .event("notification")
                            .data(String.format("[%s] %s", user, msg))
                            .build());
                } else {
                    return Mono.just(Sinks.EmitResult.FAIL_ZERO_SUBSCRIBER);
                }
            } else {
                return notificationSink.tryEmitNext(ServerSentEvent.<String>builder()
                        .id(ulid.create().toString())
                        .event("notification")
                        .data(msg)
                        .build());
            }
        }).then();
    }
}