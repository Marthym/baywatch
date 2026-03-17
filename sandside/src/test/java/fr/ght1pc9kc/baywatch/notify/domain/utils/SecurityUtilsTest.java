package fr.ght1pc9kc.baywatch.notify.domain.utils;

import fr.ght1pc9kc.baywatch.security.api.model.OneTimePassword;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Base64;

import static org.assertj.core.api.Assertions.assertThat;

class SecurityUtilsTest {

    @Test
    void should_generate_token_and_fingerprint() throws Exception {
        OneTimePassword otp = SecurityUtils.generateOneTimePassword();

        assertThat(otp.password()).isNotBlank();
        assertThat(otp.fingerprint()).isNotBlank();
        assertThat(otp.password()).matches("^[A-Za-z0-9_-]+$");
        assertThat(otp.fingerprint()).matches("^[A-Za-z0-9_-]+$");
        assertThat(otp.password()).hasSize(43);
        assertThat(otp.fingerprint()).hasSize(43);

        byte[] fingerprintBytes = MessageDigest.getInstance("SHA-256")
                .digest(otp.password().getBytes(StandardCharsets.UTF_8));
        String expectedFingerprint = Base64.getUrlEncoder().withoutPadding().encodeToString(fingerprintBytes);

        assertThat(otp.fingerprint()).isEqualTo(expectedFingerprint);
        assertThat(otp.fingerprint()).isNotEqualTo(otp.password());
    }
}
