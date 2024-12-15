package fr.ght1pc9kc.baywatch.security.infra.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import fr.ght1pc9kc.baywatch.security.api.AuthenticationFacade;
import fr.ght1pc9kc.baywatch.security.api.AuthenticationService;
import fr.ght1pc9kc.baywatch.security.api.model.AuthenticationRequest;
import fr.ght1pc9kc.baywatch.security.api.model.BaywatchAuthentication;
import fr.ght1pc9kc.baywatch.security.domain.ports.JwtTokenProvider;
import fr.ght1pc9kc.baywatch.security.infra.TokenCookieManager;
import fr.ght1pc9kc.baywatch.tests.metrics.MockObservationRegistry;
import fr.ght1pc9kc.baywatch.tests.samples.UserSamples;
import fr.ght1pc9kc.entity.jackson.EntityModule;
import fr.ght1pc9kc.testy.core.extensions.WithObjectMapper;
import graphql.GraphQLContext;
import graphql.GraphqlErrorException;
import io.micrometer.observation.ObservationRegistry;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.http.HttpCookie;
import org.springframework.http.ResponseCookie;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

class AuthenticationGqlControllerTest {
    @RegisterExtension
    private static final WithObjectMapper withObjectMapper = WithObjectMapper.builder()
            .addModule(new EntityModule())
            .build();

    private AuthenticationGqlController tested;
    private final AuthenticationService authenticationServiceMock = mock(AuthenticationService.class);
    private final TokenCookieManager tokenCookieManagerMock = mock(TokenCookieManager.class);

    @BeforeEach
    void setUp(ObjectMapper mapper) {
        BaywatchAuthentication baywatchAuthentication = new BaywatchAuthentication(UserSamples.LUKE, "fake token", false, List.of());

        JwtTokenProvider tokenProviderMock = mock(JwtTokenProvider.class);

        doReturn(Mono.just(baywatchAuthentication))
                .when(authenticationServiceMock).login(any(AuthenticationRequest.class));
        doReturn(Mono.just(baywatchAuthentication)).when(authenticationServiceMock).refresh(any());

        AuthenticationFacade authenticationFacadeMock = mock(AuthenticationFacade.class);

        doReturn(ResponseCookie.from("test").value("obiwan").build())
                .when(tokenCookieManagerMock).buildTokenCookie(any(), any());
        doReturn(Optional.of(new HttpCookie("token", "fake"))).when(tokenCookieManagerMock).getTokenCookie(any());

        ObservationRegistry observationRegistryMock = new MockObservationRegistry();
        tested = new AuthenticationGqlController(
                tokenProviderMock, authenticationServiceMock, authenticationFacadeMock, tokenCookieManagerMock,
                mapper, observationRegistryMock);
    }

    @Test
    void should_call_login_mutation() {
        MockServerWebExchange exchange = MockServerWebExchange.from(MockServerHttpRequest.post("/api/create").body(""));
        GraphQLContext qlContext = GraphQLContext.getDefault().put(ServerWebExchange.class, exchange);

        StepVerifier.create(tested.login(new AuthenticationRequest("okenobi", "mayThe4th", false), qlContext))
                .assertNext(actual -> Assertions.assertThat(actual).isInstanceOf(Map.class))
                .verifyComplete();

        verify(authenticationServiceMock).login(any());
    }

    @Test
    void should_call_logout_mutation() {
        MockServerWebExchange exchange = MockServerWebExchange.from(MockServerHttpRequest.post("/api/create").body(""));
        GraphQLContext qlContext = GraphQLContext.getDefault().put(ServerWebExchange.class, exchange);
        StepVerifier.create(tested.logout(qlContext))
                .verifyComplete();

        verify(tokenCookieManagerMock).buildTokenCookieDeletion(any());
    }

    @Test
    void should_refresh_session() {
        MockServerWebExchange exchange = MockServerWebExchange.from(MockServerHttpRequest.post("/api/create").body(""));
        GraphQLContext qlContext = GraphQLContext.getDefault().put(ServerWebExchange.class, exchange);
        StepVerifier.create(tested.refreshSession(qlContext))
                .assertNext(actual -> Assertions.assertThat(actual).isInstanceOf(Map.class))
                .verifyComplete();

        verify(authenticationServiceMock).refresh(any());
    }

    @Test
    void should_fail_not_login_refresh_session() {
        doReturn(Optional.empty()).when(tokenCookieManagerMock).getTokenCookie(any());
        MockServerWebExchange exchange = MockServerWebExchange.from(MockServerHttpRequest.post("/api/create").body(""));
        GraphQLContext qlContext = GraphQLContext.getDefault().put(ServerWebExchange.class, exchange);
        StepVerifier.create(tested.refreshSession(qlContext))
                .verifyError(GraphqlErrorException.class);

        verify(authenticationServiceMock, never()).refresh(any());
    }
}