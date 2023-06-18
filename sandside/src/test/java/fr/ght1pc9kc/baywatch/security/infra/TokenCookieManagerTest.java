package fr.ght1pc9kc.baywatch.security.infra;

import fr.ght1pc9kc.baywatch.common.api.model.Entity;
import fr.ght1pc9kc.baywatch.common.domain.Hasher;
import fr.ght1pc9kc.baywatch.security.api.model.BaywatchAuthentication;
import fr.ght1pc9kc.baywatch.security.api.model.User;
import fr.ght1pc9kc.baywatch.security.infra.model.SecurityParams;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.aggregator.AggregateWith;
import org.junit.jupiter.params.aggregator.ArgumentsAccessor;
import org.junit.jupiter.params.aggregator.ArgumentsAggregationException;
import org.junit.jupiter.params.aggregator.ArgumentsAggregator;
import org.junit.jupiter.params.converter.ArgumentConversionException;
import org.junit.jupiter.params.converter.ConvertWith;
import org.junit.jupiter.params.converter.SimpleArgumentConverter;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.http.HttpCookie;
import org.springframework.http.ResponseCookie;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;

import java.time.Duration;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

class TokenCookieManagerTest {

    private TokenCookieManager tested;

    @BeforeEach
    void setUp() {
        SecurityParams securityParams = new SecurityParams(
                new SecurityParams.JwtParams(Duration.ofMinutes(30)),
                new SecurityParams.CookieParams("X-TOKEN", Duration.ofHours(2)));
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
                new Entity<>(Hasher.sha3(User.ANONYMOUS.mail), Entity.NO_ONE, Instant.EPOCH, User.ANONYMOUS),
                "FAKE_TOKEN", rememberMe, Collections.emptyList());
        ResponseCookie actual = tested.buildTokenCookie(scheme, mockAuth);
        Assertions.assertThat(actual.toString()).containsPattern(expected);
    }

    @ParameterizedTest
    @CsvSource(delimiter = '|', value = {
            "https | X-TOKEN=; Path=/; Max-Age=0; Expires=Thu, 01 Jan 1970 00:00:00 GMT; Secure; HttpOnly; SameSite=Strict" +
                    "| X-TOKEN=; Path=/api; Max-Age=0; Expires=Thu, 01 Jan 1970 00:00:00 GMT; Secure; HttpOnly; SameSite=Strict",
            "http | X-TOKEN=; Path=/; Max-Age=0; Expires=Thu, 01 Jan 1970 00:00:00 GMT; HttpOnly; SameSite=Strict" +
                    "| X-TOKEN=; Path=/api; Max-Age=0; Expires=Thu, 01 Jan 1970 00:00:00 GMT; HttpOnly; SameSite=Strict",
    })
    void should_delete_token_cookie(String scheme, @AggregateWith(VarargsAggregator.class) String... expected) {
        List<ResponseCookie> actual = tested.buildTokenCookieDeletion(scheme);
        Assertions.assertThat(actual).hasSize(2)
                .extracting(ResponseCookie::toString)
                .containsExactly(expected);
    }

    static class VarargsAggregator implements ArgumentsAggregator {
        @Override
        public Object aggregateArguments(ArgumentsAccessor accessor, ParameterContext context) throws ArgumentsAggregationException {
            return accessor.toList().stream()
                    .skip(context.getIndex())
                    .map(String::valueOf)
                    .toArray(String[]::new);
        }
    }
}