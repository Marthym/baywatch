package fr.ght1pc9kc.baywatch.security.infra;

import fr.ght1pc9kc.baywatch.security.api.model.BaywatchAuthentication;
import fr.ght1pc9kc.baywatch.security.domain.ports.JwtTokenProvider;
import fr.ght1pc9kc.baywatch.tests.samples.UserSamples;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.List;

import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

class JwtTokenAuthenticationFilterTest {
    private JwtTokenAuthenticationFilter tested;

    @BeforeEach
    void setUp() {
        JwtTokenProvider mockTokenProvider = mock(JwtTokenProvider.class);
        doReturn(true).when(mockTokenProvider).validateToken(anyString());
        doReturn(true).when(mockTokenProvider).validateToken(anyString(), anyBoolean());
        doReturn(new BaywatchAuthentication(UserSamples.OBIWAN, "dummy token", false, List.of()))
                .when(mockTokenProvider).getAuthentication(anyString());
        TokenCookieManager mockTokenCookieManager = mock(TokenCookieManager.class);
        ReactiveUserDetailsService mockUserDetailsService = mock(ReactiveUserDetailsService.class);
        tested = new JwtTokenAuthenticationFilter(mockTokenProvider, mockTokenCookieManager, mockUserDetailsService);
    }

    @Test
    void should_filter_with_header() {
        MockServerWebExchange exchange = MockServerWebExchange.from(MockServerHttpRequest.post("/api/dummy")
                .header(HttpHeaders.AUTHORIZATION, "Bearer dummyToken")
                .body(""));
        WebFilterChain mockWebFilterChain = chain -> Mono.empty();
        StepVerifier.create(tested.filter(exchange, mockWebFilterChain))
                .verifyComplete();
    }
}