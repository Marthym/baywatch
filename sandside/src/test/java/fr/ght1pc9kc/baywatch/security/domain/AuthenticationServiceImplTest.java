package fr.ght1pc9kc.baywatch.security.domain;

import fr.ght1pc9kc.baywatch.common.api.model.TemplateVariable;
import fr.ght1pc9kc.baywatch.security.api.AuthenticationFacade;
import fr.ght1pc9kc.baywatch.security.api.UserService;
import fr.ght1pc9kc.baywatch.security.api.model.AuthenticationRequest;
import fr.ght1pc9kc.baywatch.security.api.model.BaywatchAuthentication;
import fr.ght1pc9kc.baywatch.security.api.model.Role;
import fr.ght1pc9kc.baywatch.security.api.model.User;
import fr.ght1pc9kc.baywatch.security.domain.ports.AuthenticationManagerPort;
import fr.ght1pc9kc.baywatch.security.domain.ports.JwtTokenProvider;
import fr.ght1pc9kc.baywatch.security.domain.ports.MailSenderPort;
import fr.ght1pc9kc.baywatch.security.infra.adapters.SpringAuthenticationContext;
import fr.ght1pc9kc.baywatch.tests.samples.UserSamples;
import fr.ght1pc9kc.entity.api.Entity;
import fr.ght1pc9kc.juery.api.PageRequest;
import org.assertj.core.api.Assertions;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.Duration;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;

import static fr.ght1pc9kc.baywatch.security.domain.ports.MailSenderPort.MailTemplateType.PASSWORD_RESET;
import static fr.ght1pc9kc.baywatch.tests.samples.UserSamples.LUKE;
import static fr.ght1pc9kc.baywatch.tests.samples.UserSamples.OBIWAN;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AuthenticationServiceImplTest {

    private AuthenticationServiceImpl tested;
    private JwtTokenProvider tokenProviderMock;
    private UserService userServiceMock;
    private MailSenderPort mailSenderPortMock;

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

        AuthenticationFacade authenticationFacadeMock = new SpringAuthenticationContext();
        mailSenderPortMock = mock(MailSenderPort.class);

        tested = new AuthenticationServiceImpl(
                authenticationManagerPortMock, tokenProviderMock, userServiceMock, authenticationFacadeMock, mailSenderPortMock);
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

    @Test
    @SuppressWarnings("unchecked")
    void should_ask_password_reset_by_login() {
        when(userServiceMock.list(any(PageRequest.class)))
                .thenReturn(Flux.just(OBIWAN))
                .thenReturn(Flux.error(() -> new AssertionError("Second call should never be made")));
        when(mailSenderPortMock.send(any(), anyString(), any())).thenReturn(Mono.empty());

        StepVerifier.create(tested.askPasswordReset("okenobi"))
                .verifyComplete();

        ArgumentCaptor<PageRequest> pageRequestCaptor = ArgumentCaptor.forClass(PageRequest.class);
        verify(userServiceMock, times(2)).list(pageRequestCaptor.capture());

        Assertions.assertThat(pageRequestCaptor.getValue()).hasToString(
                "PageRequest[pagination=Pagination[offset=0, size=1, sort=Sort[orders=[]]], filter=EqualOperation('mail', okenobi)]");

        ArgumentCaptor<String> emailCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<EnumMap<TemplateVariable, String>> variablesCaptor = ArgumentCaptor.forClass(EnumMap.class);
        verify(mailSenderPortMock).send(eq(PASSWORD_RESET), emailCaptor.capture(), variablesCaptor.capture());

        Assertions.assertThat(emailCaptor.getValue()).isEqualTo(OBIWAN.self().mail());
        EnumMap<TemplateVariable, String> variables = variablesCaptor.getValue();
        Assertions.assertThat(variables).containsKey(TemplateVariable.TOKEN);
        Assertions.assertThat(variables.get(TemplateVariable.TOKEN)).isNotBlank();
    }

    @Test
    void should_ask_password_reset_by_email() {
        when(userServiceMock.list(any(PageRequest.class)))
                .thenReturn(Flux.empty()) // First call (by login) returns empty
                .thenReturn(Flux.just(OBIWAN)); // Second call (by email) returns user
        when(mailSenderPortMock.send(any(), anyString(), any())).thenReturn(Mono.empty());

        StepVerifier.create(tested.askPasswordReset("obi-wan@jedi.temple"))
                .verifyComplete();

        verify(userServiceMock, times(2)).list(any(PageRequest.class));

        verify(mailSenderPortMock).send(eq(PASSWORD_RESET), eq(OBIWAN.self().mail()), any());
    }

    @Test
    void should_complete_password_reset_when_user_not_found() {
        when(userServiceMock.list(any(PageRequest.class)))
                .thenReturn(Flux.empty()) // First call (by login) returns empty
                .thenReturn(Flux.empty()); // Second call (by email) returns empty

        StepVerifier.create(tested.askPasswordReset("vader@darkside.sith"))
                .verifyComplete();

        verify(userServiceMock, times(2)).list(any(PageRequest.class));
        verify(mailSenderPortMock, times(0)).send(any(), anyString(), any());
    }

    @Test
    void should_handle_mail_sending_error() {
        when(userServiceMock.list(any(PageRequest.class)))
                .thenReturn(Flux.just(OBIWAN))
                .thenReturn(Flux.empty());
        when(mailSenderPortMock.send(any(), anyString(), any()))
                .thenReturn(Mono.error(new RuntimeException("SMTP server down")));

        StepVerifier.create(tested.askPasswordReset("okenobi"))
                .verifyError(RuntimeException.class);

        verify(mailSenderPortMock).send(eq(PASSWORD_RESET), eq(OBIWAN.self().mail()), any());
    }

    @Test
    @SuppressWarnings("unchecked")
    void should_generate_different_tokens_for_multiple_requests() {
        when(userServiceMock.list(any(PageRequest.class)))
                .thenReturn(Flux.just(OBIWAN))
                .thenReturn(Flux.empty())
                .thenReturn(Flux.just(LUKE))
                .thenReturn(Flux.empty());
        when(mailSenderPortMock.send(any(), anyString(), any())).thenReturn(Mono.empty());

        StepVerifier.create(tested.askPasswordReset("okenobi")).verifyComplete();
        StepVerifier.create(tested.askPasswordReset("lskywalker")).verifyComplete();

        ArgumentCaptor<EnumMap<TemplateVariable, String>> variablesCaptor = ArgumentCaptor.forClass(EnumMap.class);
        verify(mailSenderPortMock, times(2)).send(eq(PASSWORD_RESET), anyString(), variablesCaptor.capture());

        List<EnumMap<TemplateVariable, String>> allVariables = variablesCaptor.getAllValues();
        String token1 = allVariables.get(0).get(TemplateVariable.TOKEN);
        String token2 = allVariables.get(1).get(TemplateVariable.TOKEN);

        Assertions.assertThat(token1).isNotEqualTo(token2)
                .hasSize(40);// 32 hex chars for token + 8 hex chars for signature part
        Assertions.assertThat(token2).hasSize(40);
    }

    @Test
    void should_handle_empty_email_parameter() {
        StepVerifier.create(tested.askPasswordReset(""))
                .verifyComplete();

        verify(userServiceMock, never()).list(any(PageRequest.class));
        verify(mailSenderPortMock, never()).send(any(), anyString(), any());
    }

}