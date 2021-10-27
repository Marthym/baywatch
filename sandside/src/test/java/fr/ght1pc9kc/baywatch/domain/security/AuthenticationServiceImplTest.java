package fr.ght1pc9kc.baywatch.domain.security;

import fr.ght1pc9kc.baywatch.api.UserService;
import fr.ght1pc9kc.baywatch.api.security.model.BaywatchAuthentication;
import fr.ght1pc9kc.baywatch.api.security.model.Role;
import fr.ght1pc9kc.baywatch.api.security.model.User;
import fr.ght1pc9kc.baywatch.domain.ports.JwtTokenProvider;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;

import java.util.Collection;
import java.util.Collections;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class AuthenticationServiceImplTest {

    private AuthenticationServiceImpl tested;
    private JwtTokenProvider tokenProviderMock;

    @BeforeEach
    void setUp() {
        User user = User.builder()
                .id("42")
                .login("okenobi")
                .role(Role.USER)
                .build();
        tokenProviderMock = spy(new JwtTokenProvider() {
            @Override
            public BaywatchAuthentication createToken(User userId, boolean rememberMe, Collection<String> authorities) {
                return new BaywatchAuthentication(user, "FAKE_TOKEN", rememberMe, Collections.emptyList());
            }

            @Override
            public BaywatchAuthentication getAuthentication(String token) {
                return new BaywatchAuthentication(user, token, false, Collections.emptyList());
            }

            @Override
            public boolean validateToken(String token, boolean checkExpiration) {
                return true;
            }
        });

        UserService userServiceMock = mock(UserService.class);
        when(userServiceMock.get(anyString())).thenReturn(Mono.just(user));

        tested = new AuthenticationServiceImpl(tokenProviderMock, userServiceMock);
    }

    @Test
    void should_refresh_valid_token() {
        when(tokenProviderMock.validateToken(anyString())).thenReturn(true);
        BaywatchAuthentication actual = tested.refresh("FAKE_OLD_TOKEN").block();

        Assertions.assertThat(actual).isNotNull();
        Assertions.assertThat(actual.token).isEqualTo("FAKE_OLD_TOKEN");
    }

    @Test
    void should_refresh_outdated_token() {
        when(tokenProviderMock.validateToken(anyString())).thenReturn(false);
        BaywatchAuthentication actual = tested.refresh("FAKE_OLD_TOKEN").block();

        Assertions.assertThat(actual).isNotNull();
        Assertions.assertThat(actual.token).isEqualTo("FAKE_TOKEN");
    }
}