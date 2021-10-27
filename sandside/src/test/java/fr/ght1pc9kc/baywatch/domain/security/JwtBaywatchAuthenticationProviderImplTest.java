package fr.ght1pc9kc.baywatch.domain.security;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import fr.ght1pc9kc.baywatch.api.security.model.BaywatchAuthentication;
import fr.ght1pc9kc.baywatch.api.security.model.Role;
import fr.ght1pc9kc.baywatch.api.security.model.User;
import fr.ght1pc9kc.baywatch.domain.exceptions.SecurityException;
import fr.ght1pc9kc.baywatch.domain.ports.JwtTokenProvider;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Base64;
import java.util.Collections;

class JwtBaywatchAuthenticationProviderImplTest {

    private static final String GOOD_TOKEN = "eyJhbGciOiJIUzI1NiJ9." +
            "eyJzdWIiOiI0MiIsInJvbGVzIjoiUk9MRV9VU0VSIiwiaXNzIjoiYmF5d2F0Y2hcL3NhbmRzaWRlIiwiZXhwIjoxNjE2MzU0OTMy" +
            "LCJsb2dpbiI6Im9rZW5vYmkiLCJpYXQiOjE2MTYzNTQ5MjJ9" +
            ".qj0Gfpk39DtV0YpYD7XpNk0abN5Fy27_SR3PRr555EY";
    private static final String BAD_TOKEN = "eyJhbGciOiJIUzI1NiJ9." +
            "eyJzdWIiOiI0MiIsInJvbGVzIjoiIiwiaXNzIjoiYmF5d2F0Y2gvc2FuZHNpZGUiLCJleHAiOjE2MTYzNTU5MzIsImxvZ2luIjoi" +
            "b2tlbm9iaSIsImlhdCI6MTYxNjM1NDkyMn0=" +
            ".L3oL_Zw-NvNBQ50QjEIprwlorDvk6MIV33NgQ7Ep_Kc";

    private final JwtBaywatchAuthenticationProviderImpl tested = new JwtBaywatchAuthenticationProviderImpl(
            new byte[32],
            Duration.ofSeconds(10), Clock.fixed(Instant.parse("2021-03-21T19:28:42Z"), ZoneOffset.UTC));
    private final ObjectMapper mapper = new ObjectMapper();

    @Test
    void should_create_token() throws IOException {
        User user = User.builder().id("42").login("okenobi").role(Role.USER).build();
        String actual = tested.createToken(user, false, Collections.emptyList()).getToken();

        Assertions.assertThat(actual).isNotBlank();

        byte[] decode = Base64.getDecoder().decode(actual.split("\\.")[1]);
        JsonNode node = mapper.readTree(decode);
        Assertions.assertThat(node.get("sub").asText()).isEqualTo("42");
        Assertions.assertThat(node.get("exp").asLong()).isEqualTo(1616354932);
        Assertions.assertThat(node.get("iat").asLong()).isEqualTo(1616354922);
    }

    @Test
    void should_get_authentication() {
        BaywatchAuthentication actual = tested.getAuthentication(GOOD_TOKEN);

        Assertions.assertThat(actual.token).isEqualTo(GOOD_TOKEN);
        Assertions.assertThat(actual.user).isEqualTo(User.builder().id("42").login("okenobi").role(Role.USER).build());
    }

    @Test
    void should_fail_on_bad_token() {
        Assertions.assertThatThrownBy(() -> tested.getAuthentication(BAD_TOKEN))
                .isInstanceOf(SecurityException.class);
    }

    @Test
    void should_validate_token() {
        Assertions.assertThat(tested.validateToken(GOOD_TOKEN)).isEqualTo(true);
        Assertions.assertThat(tested.validateToken(BAD_TOKEN)).isEqualTo(false);
        Assertions.assertThat(tested.validateToken(BAD_TOKEN)).isEqualTo(false);

        JwtTokenProvider testedFuture = new JwtBaywatchAuthenticationProviderImpl(
                new byte[32],
                Duration.ofSeconds(10), Clock.fixed(Instant.parse("2021-03-21T19:29:42Z"), ZoneOffset.UTC));

        Assertions.assertThat(testedFuture.validateToken(GOOD_TOKEN)).isEqualTo(false);
        Assertions.assertThat(testedFuture.validateToken(GOOD_TOKEN, false)).isEqualTo(true);
    }
}