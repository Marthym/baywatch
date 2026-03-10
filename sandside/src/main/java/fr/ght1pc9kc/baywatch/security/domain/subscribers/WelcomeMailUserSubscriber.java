package fr.ght1pc9kc.baywatch.security.domain.subscribers;

import fr.ght1pc9kc.baywatch.common.api.model.TemplateVariable;
import fr.ght1pc9kc.baywatch.security.api.model.User;
import fr.ght1pc9kc.baywatch.security.domain.ports.KeyValuePersistencePort;
import fr.ght1pc9kc.baywatch.security.domain.ports.MailSenderPort;
import fr.ght1pc9kc.baywatch.security.domain.ports.UserEventPublisherPort;
import fr.ght1pc9kc.entity.api.Entity;
import lombok.extern.slf4j.Slf4j;
import reactor.core.Disposable;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Duration;
import java.util.Base64;
import java.util.EnumMap;
import java.util.Map;
import java.util.Map.Entry;

@Slf4j
public class WelcomeMailUserSubscriber {
    private static final int TOKEN_SIZE = 32;
    private static final Duration TOKEN_TTL = Duration.ofMinutes(15);
    private static final SecureRandom RANDOM = new SecureRandom();
    private static final String TOKEN_KEY_PREFIX = "security:welcome:";
    private static final Base64.Encoder BASE64_URL_ENCODER = Base64.getUrlEncoder().withoutPadding();

    private final MailSenderPort mailSender;
    private final KeyValuePersistencePort keyValuePersistencePort;
    private final Disposable disposable;

    public WelcomeMailUserSubscriber(
            UserEventPublisherPort userPublisher, MailSenderPort mailSender, KeyValuePersistencePort keyValuePersistencePort) {
        this.mailSender = mailSender;
        this.disposable = userPublisher.onEvent(this::onNewUser);
        this.keyValuePersistencePort = keyValuePersistencePort;
        log.debug("WelcomeMailUserSubscriber started.");
    }

    private Mono<Void> onNewUser(Entity<User> user) {
        Entry<String, String> token = generateToken();
        EnumMap<TemplateVariable, String> variables = new EnumMap<>(TemplateVariable.class);
        variables.put(TemplateVariable.TOKEN, token.getValue());
        variables.put(TemplateVariable.USERNAME, user.self().login());

        keyValuePersistencePort.store(TOKEN_KEY_PREFIX + token.getKey(), user, TOKEN_TTL);

        return mailSender.send(MailSenderPort.MailTemplateType.WELCOME_USER, user.self().mail(), variables);
    }

    private Entry<String, String> generateToken() {
        try {
            byte[] tokenBytes = new byte[TOKEN_SIZE];
            RANDOM.nextBytes(tokenBytes);
            String token = BASE64_URL_ENCODER.encodeToString(tokenBytes);

            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] keyBytes = digest.digest(token.getBytes(StandardCharsets.UTF_8));
            String key = BASE64_URL_ENCODER.encodeToString(keyBytes);

            return Map.entry(key, token);
        } catch (NoSuchAlgorithmException e) {
            throw new SecurityException("Unable to generate reset password token", e);
        }
    }

    public void destroy() {
        this.disposable.dispose();
    }
}
