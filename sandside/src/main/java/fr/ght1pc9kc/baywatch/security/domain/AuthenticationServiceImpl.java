package fr.ght1pc9kc.baywatch.security.domain;

import fr.ght1pc9kc.baywatch.common.api.model.UserMeta;
import fr.ght1pc9kc.baywatch.security.api.AuthenticationFacade;
import fr.ght1pc9kc.baywatch.security.api.AuthenticationService;
import fr.ght1pc9kc.baywatch.security.api.UserService;
import fr.ght1pc9kc.baywatch.security.api.model.AuthenticationRequest;
import fr.ght1pc9kc.baywatch.security.api.model.BaywatchAuthentication;
import fr.ght1pc9kc.baywatch.security.api.model.User;
import fr.ght1pc9kc.baywatch.security.domain.ports.AuthenticationManagerPort;
import fr.ght1pc9kc.baywatch.security.domain.ports.JwtTokenProvider;
import fr.ght1pc9kc.entity.api.Entity;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Clock;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

@Slf4j
@RequiredArgsConstructor
public class AuthenticationServiceImpl implements AuthenticationService {
    private final AuthenticationManagerPort authenticationManagerPort;
    private final JwtTokenProvider tokenProvider;
    private final UserService userService;
    private final AuthenticationFacade authFacade;
    private final Clock clock;
    private final Duration autoUpdateUserDelay;

    private final Queue<Entity<User>> toUpdate = new ConcurrentLinkedQueue<>();
    private final ScheduledExecutorService persistExecutor = Executors.newSingleThreadScheduledExecutor(r ->
            new Thread(r, "user-persist-scheduler"));
    private final AtomicBoolean running = new AtomicBoolean(false);

    private void schedulePersistence() {
        if (running.compareAndSet(false, true)) {
            persistExecutor.schedule(() -> Flux.<Entity<User>>create(sink -> {
                        Entity<User> entity;
                        while ((entity = toUpdate.poll()) != null) {
                            sink.next(entity);
                        }
                        sink.complete();
                    })
                    .map(entity -> entity.convert(u -> u.withPassword(null)))
                    .distinct(Entity::id)
                    .concatMap(u -> userService.update(u)
                            .contextWrite(authFacade.withAuthentication(u))
                            .onErrorResume(e -> {
                                log.atWarn()
                                        .addArgument(e.getClass())
                                        .addArgument(e.getLocalizedMessage())
                                        .log("Error while updating user -> {}: {}");
                                return Mono.empty();
                            }))
                    .onErrorResume(e -> {
                        log.atWarn()
                                .addArgument(e.getClass())
                                .addArgument(e.getLocalizedMessage())
                                .log("Error while draining user queue -> {}: {}");
                        return Mono.empty();
                    })
                    .doFinally(ignore -> {
                        running.set(false);
                        log.atDebug().log("User persistence complete.");
                    })
                    .subscribe(), autoUpdateUserDelay.toMillis(), TimeUnit.MILLISECONDS);
        }
    }

    @Override
    public Mono<BaywatchAuthentication> login(AuthenticationRequest login) {
        return authenticationManagerPort.authenticate(login)
                .map(auth -> tokenProvider.createToken(auth.user(), auth.rememberMe(), auth.authorities()))
                .doOnNext(auth -> {
                    toUpdate.offer(auth.user());
                    schedulePersistence();
                    log.atDebug()
                            .addArgument(auth.user().self().login())
                            .log("Login {} successful");
                });
    }

    @Override
    public Mono<BaywatchAuthentication> refresh(String token) {
        return Mono.fromCallable(() -> tokenProvider.getAuthentication(token))
                .flatMap(auth -> userService.get(auth.user().id())
                        .map(user -> tokenProvider.createToken(user, auth.rememberMe(), Collections.emptyList())))
                .doOnSuccess(auth -> {
                    toUpdate.offer(auth.user().withMeta(UserMeta.loginAt, clock.instant().truncatedTo(ChronoUnit.SECONDS)));
                    schedulePersistence();
                    log.atDebug()
                            .addArgument(auth.user().self().login())
                            .log("Refresh {} successful");
                });
    }

    @PreDestroy
    @SneakyThrows
    public void shutdown() {
        if (!persistExecutor.isShutdown()) {
            log.atInfo().log("Commencing graceful shutdown. ⏳ Waiting for user persistence to complete");
            persistExecutor.shutdown();
            if (!persistExecutor.awaitTermination(5, TimeUnit.MINUTES)) {
                log.atWarn().log("User persistence shutdown timeout ! Some users information can be lost \uD83D\uDE31 !");
            }
        }
    }
}