package fr.ght1pc9kc.baywatch.domain.security;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import fr.ght1pc9kc.baywatch.api.common.model.Entity;
import fr.ght1pc9kc.baywatch.api.security.model.BaywatchAuthentication;
import fr.ght1pc9kc.baywatch.api.security.model.Role;
import fr.ght1pc9kc.baywatch.api.security.model.User;
import fr.ght1pc9kc.baywatch.domain.security.exceptions.SecurityException;
import fr.ght1pc9kc.baywatch.domain.security.ports.JwtTokenProvider;
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
            "eyJyZW1lbWJlciI6ZmFsc2UsInN1YiI6IjQyIiwiY3JlYXRlZEF0IjowLCJyb2xlcyI6IlJPTEVfVVNFUiIsImlzcyI6ImJheXdhd" +
            "GNoXC9zYW5kc2lkZSIsImV4cCI6MTYxNjM1NDkzMiwibG9naW4iOiJva2Vub2JpIiwiaWF0IjoxNjE2MzU0OTIyfQ" +
            ".6dFZCpf1hPpwuzW-l7pikgFQCXvNielj62r3L_jmVug";
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
        Entity<User> user = new Entity<>("42", Instant.EPOCH, User.builder().login("okenobi").role(Role.USER).build());
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
        Assertions.assertThat(actual.user).isEqualTo(
                new Entity<>("42", Instant.EPOCH, User.builder().login("okenobi").role(Role.USER).build()));
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