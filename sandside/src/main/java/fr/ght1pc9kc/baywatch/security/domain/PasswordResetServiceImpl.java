package fr.ght1pc9kc.baywatch.security.domain;

import fr.ght1pc9kc.baywatch.common.api.model.TemplateVariable;
import fr.ght1pc9kc.baywatch.security.api.AuthenticationFacade;
import fr.ght1pc9kc.baywatch.security.api.PasswordChecker;
import fr.ght1pc9kc.baywatch.security.api.PasswordResetService;
import fr.ght1pc9kc.baywatch.security.api.UserService;
import fr.ght1pc9kc.baywatch.security.api.model.User;
import fr.ght1pc9kc.baywatch.security.domain.exceptions.InvalidTokenException;
import fr.ght1pc9kc.baywatch.security.domain.exceptions.PasswordEvaluationException;
import fr.ght1pc9kc.baywatch.security.domain.ports.KeyValuePersistencePort;
import fr.ght1pc9kc.baywatch.security.domain.ports.MailSenderPort;
import fr.ght1pc9kc.entity.api.Entity;
import fr.ght1pc9kc.juery.api.Criteria;
import fr.ght1pc9kc.juery.api.PageRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Duration;
import java.util.Base64;
import java.util.EnumMap;
import java.util.Map;
import java.util.NoSuchElementException;

import static fr.ght1pc9kc.baywatch.common.api.model.BaywatchLogsMarkers.AUDIT;
import static fr.ght1pc9kc.baywatch.common.api.model.EntitiesProperties.LOGIN;
import static fr.ght1pc9kc.baywatch.common.api.model.EntitiesProperties.MAIL;
import static fr.ght1pc9kc.baywatch.security.domain.ports.MailSenderPort.MailTemplateType.PASSWORD_RESET;

@Slf4j
@RequiredArgsConstructor
public class PasswordResetServiceImpl implements PasswordResetService {
    private static final int TOKEN_SIZE = 32;
    private static final Duration TOKEN_TTL = Duration.ofMinutes(15);
    private static final SecureRandom RANDOM = new SecureRandom();
    private static final String TOKEN_KEY_PREFIX = KeyValuePersistencePort.RESET_PASSWORD_PREFIX;
    private static final Base64.Encoder BASE64_URL_ENCODER = Base64.getUrlEncoder().withoutPadding();

    private final AuthenticationFacade authFacade;
    private final UserService userService;
    private final PasswordChecker passwordChecker;
    private final MailSenderPort mailSenderPort;
    private final KeyValuePersistencePort keyValuePersistencePort;

    @Override
    @SuppressWarnings("ConstantValue")
    public Mono<Void> askPasswordReset(@NotNull String email) {
        if (email.isBlank()) {
            return Mono.empty().then();
        }
        return userService.list(PageRequest.one(Criteria.property(LOGIN).eq(email)))
                .contextWrite(AuthenticationFacade.withSystemAuthentication())
                .switchIfEmpty(userService.list(PageRequest.one(Criteria.property(MAIL).eq(email)))
                        .contextWrite(AuthenticationFacade.withSystemAuthentication()))
                .single()
                .doOnSuccess(user -> log.atInfo().addArgument(user.self().login())
                        .log("Send password reset to {} successfully"))
                .doOnError(NoSuchElementException.class, _ ->
                        log.atWarn().addArgument(email).log("No user found for email: {}"))
                .doOnError(IndexOutOfBoundsException.class, _ ->
                        log.atWarn().addArgument(email).log("More than one user found for email: {}"))
                .onErrorComplete()
                .map(this::generateToken)
                .flatMap(this::sendPasswordResetMail)
                .then();
    }

    private Tuple2<Entity<User>, String> generateToken(Entity<User> user) {
        try {
            byte[] tokenBytes = new byte[TOKEN_SIZE];
            RANDOM.nextBytes(tokenBytes);
            String token = BASE64_URL_ENCODER.encodeToString(tokenBytes);

            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] keyBytes = digest.digest(token.getBytes(StandardCharsets.UTF_8));
            String key = BASE64_URL_ENCODER.encodeToString(keyBytes);

            keyValuePersistencePort.store(TOKEN_KEY_PREFIX + key, user, TOKEN_TTL);
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

    public Mono<Void> resetPassword(@NotNull String token, @NotNull String newPassword) {
        return Mono.<String>create(sink -> {
                    try {
                        MessageDigest digest = MessageDigest.getInstance("SHA-256");
                        byte[] keyBytes = digest.digest(token.getBytes(StandardCharsets.UTF_8));
                        sink.success(BASE64_URL_ENCODER.encodeToString(keyBytes));
                    } catch (NoSuchAlgorithmException e) {
                        sink.error(new SecurityException("Unable to reset password", e));
                    }

                }).flatMap(key -> {
                    Entity<User> user = keyValuePersistencePort.get(TOKEN_KEY_PREFIX + key)
                            .map(e -> e.convert(u -> u.withPassword(newPassword)))
                            .orElseThrow(() -> new InvalidTokenException("Invalid or expired token !"));
                    return passwordChecker.checkPasswordStrength(user.self())
                            .<Entity<User>>handle((eval, sink) -> {
                                if (eval.isSecure()) {
                                    keyValuePersistencePort.remove(TOKEN_KEY_PREFIX + key);
                                    sink.next(user);
                                } else {
                                    sink.error(new PasswordEvaluationException(eval.message()));
                                }
                            });

                }).flatMap(user ->
                        userService.update(user)
                                .contextWrite(AuthenticationFacade.withSystemAuthentication()))

                .doOnSuccess(u -> log.atWarn().addMarker(AUDIT)
                        .addKeyValue("operator", u.id())
                        .addKeyValue("type", "User")
                        .addKeyValue("action", "resetPassword")
                        .addKeyValue("id", u.id())
                        .addArgument(u.self().login())
                        .log("User {} reset password successfully."))
                .then();
    }
}
