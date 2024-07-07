package fr.ght1pc9kc.baywatch.security.domain;

import fr.ght1pc9kc.baywatch.security.api.AuthenticationFacade;
import fr.ght1pc9kc.baywatch.security.api.UserService;
import fr.ght1pc9kc.baywatch.security.api.model.AuthenticationRequest;
import fr.ght1pc9kc.baywatch.security.api.model.BaywatchAuthentication;
import fr.ght1pc9kc.baywatch.security.api.model.Role;
import fr.ght1pc9kc.baywatch.security.api.model.User;
import fr.ght1pc9kc.baywatch.security.domain.ports.AuthenticationManagerPort;
import fr.ght1pc9kc.baywatch.security.domain.ports.JwtTokenProvider;
import fr.ght1pc9kc.baywatch.tests.samples.UserSamples;
import fr.ght1pc9kc.entity.api.Entity;
import org.assertj.core.api.Assertions;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.Duration;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AuthenticationServiceImplTest {

    private AuthenticationServiceImpl tested;
    private JwtTokenProvider tokenProviderMock;
    private UserService userServiceMock;

    @BeforeEach
    void setUp() {
        Entity<User> user = Entity.identify(User.builder().login("okenobi").role(Role.USER).build()).withId("42");
        tokenProviderMock = spy(new JwtTokenProvider() {
            @Override
            public BaywatchAuthentication createToken(Entity<User> userId, boolean rememberMe, Collection<String> authorities) {
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

        AuthenticationManagerPort authenticationManagerPortMock = mock(AuthenticationManagerPort.class);
        doReturn(Mono.just(new BaywatchAuthentication(UserSamples.LUKE, "dummy token", true, List.of())))
                .when(authenticationManagerPortMock).authenticate(any());

        userServiceMock = mock(UserService.class);
        when(userServiceMock.get(anyString())).thenReturn(Mono.just(user));

        AuthenticationFacade authenticationFacadeMock = mock(AuthenticationFacade.class);
        tested = new AuthenticationServiceImpl(authenticationManagerPortMock, tokenProviderMock, userServiceMock, authenticationFacadeMock);
    }

    @Test
    void should_refresh_valid_token() {
        when(tokenProviderMock.validateToken(anyString())).thenReturn(true);
        BaywatchAuthentication actual = tested.refresh("FAKE_OLD_TOKEN").block();

        Assertions.assertThat(actual).isNotNull();
        Assertions.assertThat(actual.token()).isEqualTo("FAKE_TOKEN");
    }

    @Test
    void should_refresh_outdated_token() {
        when(tokenProviderMock.validateToken(anyString())).thenReturn(false);
        BaywatchAuthentication actual = tested.refresh("FAKE_OLD_TOKEN").block();

        Assertions.assertThat(actual).isNotNull();
        Assertions.assertThat(actual.token()).isEqualTo("FAKE_TOKEN");
    }

    @Test
    void should_call_update_after_post_construct() {
        tested.onPostConstruct();

        StepVerifier.create(tested.login(new AuthenticationRequest("okenobi", "MayThe4th", true)))
                .assertNext(actual -> Assertions.assertThat(actual.user().id()).isEqualTo("42"))
                .verifyComplete();
        Awaitility.await().atMost(Duration.ofSeconds(5)).untilAsserted(() -> verify(userServiceMock).update(any()));
    }
}