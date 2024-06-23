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
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.VisibleForTesting;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;

import java.time.Clock;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.Collections;

@Slf4j
@RequiredArgsConstructor
public class AuthenticationServiceImpl implements AuthenticationService {
    private final AuthenticationManagerPort authenticationManagerPort;
    private final JwtTokenProvider tokenProvider;
    private final UserService userService;
    private final AuthenticationFacade authFacade;

    private final Sinks.Many<Entity<User>> toUpdate = Sinks.many().unicast().onBackpressureBuffer();

    @Setter(value = AccessLevel.PACKAGE, onMethod = @__(@VisibleForTesting))
    private Clock clock = Clock.systemUTC();

    public void onPostConstruct() {
        log.atDebug().log("Subscribe to user update...");
        toUpdate.asFlux()
                .map(entity -> entity.convert(u -> u.withPassword(null)))
                .bufferTimeout(5, Duration.ofSeconds(2))
                .flatMap(users -> Flux.merge(users.stream()
                        .map(u -> userService.update(u)
                                .contextWrite(authFacade.withAuthentication(u)))
                        .toList()).then())
                .subscribe();
        log.atDebug().log("Subscribe to user update");
    }

    @Override
    public Mono<BaywatchAuthentication> login(AuthenticationRequest login) {
        return authenticationManagerPort.authenticate(login)
                .map(auth -> tokenProvider.createToken(auth.user(), auth.rememberMe(), auth.authorities()))
                .doOnNext(auth -> {
                    toUpdate.tryEmitNext(auth.user());
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
                    toUpdate.tryEmitNext(auth.user().withMeta(UserMeta.loginAt, clock.instant().truncatedTo(ChronoUnit.SECONDS)));
                    log.atDebug()
                            .addArgument(auth.user().self().login())
                            .log("Refresh {} successful");
                });
    }
}