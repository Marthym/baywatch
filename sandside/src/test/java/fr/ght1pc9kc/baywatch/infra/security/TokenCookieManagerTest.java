package fr.ght1pc9kc.baywatch.infra.security;

import fr.ght1pc9kc.baywatch.api.common.model.Entity;
import fr.ght1pc9kc.baywatch.api.security.model.BaywatchAuthentication;
import fr.ght1pc9kc.baywatch.api.security.model.User;
import fr.ght1pc9kc.baywatch.domain.common.Hasher;
import fr.ght1pc9kc.baywatch.infra.security.model.SecurityParams;
import fr.ght1pc9kc.baywatch.infra.security.model.SecurityParams.CookieParams;
import fr.ght1pc9kc.baywatch.infra.security.model.SecurityParams.JwtParams;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.http.HttpCookie;
import org.springframework.http.ResponseCookie;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;

import java.time.Duration;
import java.time.Instant;
import java.util.Collections;
import java.util.Optional;

class TokenCookieManagerTest {

    private TokenCookieManager tested;

    @BeforeEach
    void setUp() {
        SecurityParams securityParams = new SecurityParams(
                new JwtParams(Duration.ofMinutes(30)),
                new CookieParams("X-TOKEN", Duration.ofHours(2)));
        tested = new TokenCookieManager("/api", securityParams);
    }

    @Test
    void should_get_cookie_from_request() {
        {
            MockServerHttpRequest mockRequest = MockServerHttpRequest.get("https://www.ght1pc9kc.fr/api/news")
                    .cookie(new HttpCookie("X-TOKEN", "FAKE_COOKIE"))
                    .build();
            Optional<HttpCookie> actual = tested.getTokenCookie(mockRequest);
            Assertions.assertThat(actual).isNotEmpty()
                    .contains(new HttpCookie("X-TOKEN", "FAKE_COOKIE"));
        }
        {
            MockServerHttpRequest mockRequest = MockServerHttpRequest.get("https://www.ght1pc9kc.fr/api/news")
                    .build();
            Optional<HttpCookie> actual = tested.getTokenCookie(mockRequest);
            Assertions.assertThat(actual).isEmpty();
        }
    }

    @ParameterizedTest
    @CsvSource({
            "https, true, X-TOKEN=FAKE_TOKEN; Path=/api; Secure; HttpOnly; SameSite=Strict",
            "https, false, X-TOKEN=FAKE_TOKEN; Path=/api; Max-Age=7200; Expires=[^;]*; Secure; HttpOnly; SameSite=Strict",
            "http, false, X-TOKEN=FAKE_TOKEN; Path=/api; Max-Age=7200; Expires=[^;]*; HttpOnly; SameSite=Strict",
            "http, true, X-TOKEN=FAKE_TOKEN; Path=/api; HttpOnly; SameSite=Strict",
    })
    void should_build_token_cookie(String scheme, boolean rememberMe, String expected) {
        BaywatchAuthentication mockAuth = new BaywatchAuthentication(
                new Entity<>(Hasher.sha3(User.ANONYMOUS.mail), Instant.EPOCH, User.ANONYMOUS),
                "FAKE_TOKEN", rememberMe, Collections.emptyList());
        ResponseCookie actual = tested.buildTokenCookie(scheme, mockAuth);
        Assertions.assertThat(actual.toString()).containsPattern(expected);
    }

    @ParameterizedTest
    @CsvSource(delimiter = '|', value = {
            "https | X-TOKEN=; Path=/api; Max-Age=0; Expires=Thu, 01 Jan 1970 00:00:00 GMT; Secure; HttpOnly; SameSite=Strict",
            "http | X-TOKEN=; Path=/api; Max-Age=0; Expires=Thu, 01 Jan 1970 00:00:00 GMT; HttpOnly; SameSite=Strict",
    })
    void should_delete_token_cookie(String scheme, String expected) {
        ResponseCookie actual = tested.buildTokenCookieDeletion(scheme);
        Assertions.assertThat(actual.toString()).isEqualTo(expected);
    }
}