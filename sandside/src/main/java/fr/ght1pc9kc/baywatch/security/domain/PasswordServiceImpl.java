package fr.ght1pc9kc.baywatch.security.domain;

import fr.ght1pc9kc.baywatch.common.api.ClientInfoFacade;
import fr.ght1pc9kc.baywatch.common.api.exceptions.UnauthorizedException;
import fr.ght1pc9kc.baywatch.security.api.AuthenticationFacade;
import fr.ght1pc9kc.baywatch.security.api.PasswordChecker;
import fr.ght1pc9kc.baywatch.security.api.model.OneTimePassword;
import fr.ght1pc9kc.baywatch.security.api.model.PasswordEvaluation;
import fr.ght1pc9kc.baywatch.security.api.model.User;
import fr.ght1pc9kc.baywatch.security.domain.ports.PasswordStrengthChecker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.List;
import java.util.Objects;
import java.util.stream.LongStream;
import java.util.stream.Stream;

import static java.util.function.Predicate.not;

@Slf4j
@RequiredArgsConstructor
public class PasswordServiceImpl implements PasswordChecker {
    private static final int TOKEN_SIZE_BYTES = 32;
    private static final Base64.Encoder BASE64_URL_ENCODER = Base64.getUrlEncoder().withoutPadding();
    private static final SecureRandom SECURE_RANDOM = createSecureRandom();

    private final AuthenticationFacade authFacade;
    private final PasswordStrengthChecker passwordChecker;
    private final ClientInfoFacade clientInfoFacade;

    @Override
    public Mono<PasswordEvaluation> checkPasswordStrength(String password) {
        return authFacade.getConnectedUser()
                .switchIfEmpty(Mono.error(UnauthorizedException::new))
                .map(user -> user.self().withPassword(password))
                .flatMap(this::checkPasswordStrength);
    }

    @Override
    public Mono<PasswordEvaluation> checkPasswordStrength(User user) {
        List<String> dictionary = Stream.of(user.name(), user.login(), user.mail())
                .filter(not(Objects::isNull))
                .toList();
        return clientInfoFacade.getLocale()
                .map(locale -> passwordChecker.estimate(user.password(), locale, dictionary));
    }

    @Override
    public Flux<String> generateSecurePassword(int number) {
        if (number > 100 || number < 1) {
            return Flux.error(() -> new IllegalArgumentException("Invalid number of passwords required !"));
        }
        return Flux.<String>create(sink ->
                        sink.onRequest(n -> LongStream.range(0, number)
                                .mapToObj(ignore -> passwordChecker.generate())
                                .forEach(sink::next)))
                .take(number);
    }

    public OneTimePassword generateOneTimePassword() {
        byte[] tokenBytes = new byte[TOKEN_SIZE_BYTES];
        SECURE_RANDOM.nextBytes(tokenBytes);
        String token = BASE64_URL_ENCODER.encodeToString(tokenBytes);
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] fingerprintBytes = digest.digest(token.getBytes(StandardCharsets.UTF_8));
            String fingerprint = BASE64_URL_ENCODER.encodeToString(fingerprintBytes);
            return new OneTimePassword(token, fingerprint);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 algorithm not available", e);
        }
    }

    private static SecureRandom createSecureRandom() {
        try {
            return SecureRandom.getInstanceStrong();
        } catch (NoSuchAlgorithmException e) {
            log.warn("Strong SecureRandom unavailable, falling back to default", e);
            return new SecureRandom();
        }
    }
}
