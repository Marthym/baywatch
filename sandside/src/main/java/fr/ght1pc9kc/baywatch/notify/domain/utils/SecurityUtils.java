package fr.ght1pc9kc.baywatch.notify.domain.utils;

import fr.ght1pc9kc.baywatch.security.api.model.OneTimePassword;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

@Slf4j
@UtilityClass
public class SecurityUtils {
    private static final int TOKEN_SIZE_BYTES = 32;
    private static final Base64.Encoder BASE64_URL_ENCODER = Base64.getUrlEncoder().withoutPadding();
    private static final SecureRandom SECURE_RANDOM = createSecureRandom();

    private static SecureRandom createSecureRandom() {
        try {
            return SecureRandom.getInstanceStrong();
        } catch (NoSuchAlgorithmException e) {
            log.warn("Strong SecureRandom unavailable, falling back to default", e);
            return new SecureRandom();
        }
    }

    public static OneTimePassword generateOneTimePassword() {
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
}
