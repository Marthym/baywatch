package fr.ght1pc9kc.baywatch.security.domain;

import fr.ght1pc9kc.baywatch.common.api.model.TemplateVariable;
import fr.ght1pc9kc.baywatch.common.api.model.UserMeta;
import fr.ght1pc9kc.baywatch.security.api.AuthenticationFacade;
import fr.ght1pc9kc.baywatch.security.api.AuthenticationService;
import fr.ght1pc9kc.baywatch.security.api.UserService;
import fr.ght1pc9kc.baywatch.security.api.model.AuthenticationRequest;
import fr.ght1pc9kc.baywatch.security.api.model.BaywatchAuthentication;
import fr.ght1pc9kc.baywatch.security.api.model.User;
import fr.ght1pc9kc.baywatch.security.domain.ports.AuthenticationManagerPort;
import fr.ght1pc9kc.baywatch.security.domain.ports.JwtTokenProvider;
import fr.ght1pc9kc.baywatch.security.domain.ports.MailSenderPort;
import fr.ght1pc9kc.baywatch.security.domain.ports.ResetPasswordTokenPort;
import fr.ght1pc9kc.entity.api.Entity;
import fr.ght1pc9kc.juery.api.Criteria;
import fr.ght1pc9kc.juery.api.PageRequest;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.VisibleForTesting;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Clock;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;

import static fr.ght1pc9kc.baywatch.common.api.model.EntitiesProperties.LOGIN;
import static fr.ght1pc9kc.baywatch.common.api.model.EntitiesProperties.MAIL;
import static fr.ght1pc9kc.baywatch.security.domain.ports.MailSenderPort.MailTemplateType.PASSWORD_RESET;
import static java.util.Objects.nonNull;

@Slf4j
@RequiredArgsConstructor
public class AuthenticationServiceImpl implements AuthenticationService {
    private static final SecureRandom RANDOM = new SecureRandom();
    private static final Base64.Encoder BASE64_URL_ENCODER = Base64.getUrlEncoder().withoutPadding();

    private final AuthenticationManagerPort authenticationManagerPort;
    private final JwtTokenProvider tokenProvider;
    private final UserService userService;
    private final AuthenticationFacade authFacade;
    private final MailSenderPort mailSenderPort;
    private final ResetPasswordTokenPort resetPasswordTokenPort;

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
                .onErrorResume(e -> {
                    log.atWarn()
                            .addArgument(e.getClass())
                            .addArgument(e.getLocalizedMessage())
                            .log("Error while updating user -> {}: {}");
                    return Mono.empty().then();
                })
                .subscribe();
        log.atDebug().log("Subscribed to user update");
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

    @Override
    public Mono<Void> askPasswordReset(@NotNull String email) {
        if (email.isBlank()) {
            return Mono.empty().then();
        }
        return userService.list(PageRequest.one(Criteria.property(LOGIN).eq(email)))
                .contextWrite(AuthenticationFacade.withSystemAuthentication())
                .switchIfEmpty(userService.list(PageRequest.one(Criteria.property(MAIL).eq(email)))
                        .contextWrite(AuthenticationFacade.withSystemAuthentication()))
                .next()
                .map(this::generateToken)
                .flatMap(this::sendPasswordResetMail)
                .doOnSuccess(user -> {
                    if (nonNull(user)) {
                        log.atInfo().addArgument(user.self().login())
                                .log("Send password reset to {} successful");
                    }
                }).then();
    }

    private Tuple2<Entity<User>, String> generateToken(Entity<User> user) {
        try {
            byte[] tokenBytes = new byte[32];
            RANDOM.nextBytes(tokenBytes);
            String token = BASE64_URL_ENCODER.encodeToString(tokenBytes);

            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] keyBytes = digest.digest(tokenBytes);
            String key = BASE64_URL_ENCODER.encodeToString(keyBytes);

            resetPasswordTokenPort.store(key, user);
            return Tuples.of(user, token);
        } catch (NoSuchAlgorithmException e) {
            throw new SecurityException("Unable to generate reset password token", e);
        }
    }

    private Mono<Entity<User>> sendPasswordResetMail(Tuple2<Entity<User>, String> tuple) {
        Entity<User> user = tuple.getT1();
        EnumMap<TemplateVariable, String> variables = new EnumMap<>(Map.of(TemplateVariable.TOKEN, tuple.getT2()));
        return mailSenderPort.send(PASSWORD_RESET, user.self().mail(), variables)
                .contextWrite(authFacade.withAuthentication(user))
                .thenReturn(user);
    }
}