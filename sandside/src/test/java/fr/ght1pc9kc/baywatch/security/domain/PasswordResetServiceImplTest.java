package fr.ght1pc9kc.baywatch.security.domain;

import fr.ght1pc9kc.baywatch.common.api.ClientInfoFacade;
import fr.ght1pc9kc.baywatch.common.api.model.TemplateVariable;
import fr.ght1pc9kc.baywatch.security.api.AuthenticationFacade;
import fr.ght1pc9kc.baywatch.security.api.PasswordChecker;
import fr.ght1pc9kc.baywatch.security.api.PasswordResetService;
import fr.ght1pc9kc.baywatch.security.api.UserService;
import fr.ght1pc9kc.baywatch.security.api.model.PasswordEvaluation;
import fr.ght1pc9kc.baywatch.security.api.model.User;
import fr.ght1pc9kc.baywatch.security.domain.exceptions.PasswordEvaluationException;
import fr.ght1pc9kc.baywatch.security.domain.ports.KeyValuePersistencePort;
import fr.ght1pc9kc.baywatch.security.domain.ports.MailSenderPort;
import fr.ght1pc9kc.baywatch.tests.samples.UserSamples;
import fr.ght1pc9kc.juery.api.PageRequest;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import reactor.util.context.Context;

import java.util.EnumMap;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

import static fr.ght1pc9kc.baywatch.security.domain.ports.MailSenderPort.MailTemplateType.PASSWORD_RESET;
import static fr.ght1pc9kc.baywatch.tests.samples.UserSamples.DSIDIOUS;
import static fr.ght1pc9kc.baywatch.tests.samples.UserSamples.LUKE;
import static fr.ght1pc9kc.baywatch.tests.samples.UserSamples.OBIWAN;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.assertArg;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class PasswordResetServiceImplTest {

    private PasswordResetService tested;

    private MailSenderPort mailSenderPortMock;
    private UserService userServiceMock;
    private KeyValuePersistencePort keyValuePersistencePortMock;
    private PasswordChecker passwordCheckerMock;

    @BeforeEach
    void setUp() {
        AuthenticationFacade authenticationFacade = mock(AuthenticationFacade.class);
        when(authenticationFacade.getConnectedUser()).thenReturn(Mono.just(UserSamples.OBIWAN));
        when(authenticationFacade.withAuthentication(any())).thenReturn(Context.empty());

        ClientInfoFacade clientInfoFacade = mock(ClientInfoFacade.class);
        when(clientInfoFacade.getLocale()).thenReturn(Mono.just(Locale.ENGLISH));

        passwordCheckerMock = mock(PasswordChecker.class);
        when(passwordCheckerMock.checkPasswordStrength(OBIWAN.self()))
                .thenReturn(Mono.just(new PasswordEvaluation(true, 65d, "ok")));
        when(passwordCheckerMock.checkPasswordStrength(DSIDIOUS.self()))
                .thenReturn(Mono.just(new PasswordEvaluation(false, 2d, "Not strong enough")));

        mailSenderPortMock = mock(MailSenderPort.class);

        userServiceMock = mock(UserService.class);
        when(userServiceMock.get(anyString())).thenReturn(Mono.just(OBIWAN));
        when(userServiceMock.list(any(PageRequest.class))).thenReturn(Flux.just(OBIWAN));
        when(userServiceMock.update(any())).thenReturn(Mono.just(OBIWAN));

        keyValuePersistencePortMock = mock(KeyValuePersistencePort.class);
        when(keyValuePersistencePortMock.get(anyString())).thenReturn(Optional.of(OBIWAN));

        tested = new PasswordResetServiceImpl(
                authenticationFacade, userServiceMock, passwordCheckerMock, mailSenderPortMock, keyValuePersistencePortMock);
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

        Assertions.assertThat(token1).isNotEqualTo(token2).hasSize(43);
        Assertions.assertThat(token2).hasSize(43);
    }

    @Test
    void should_handle_empty_email_parameter() {
        StepVerifier.create(tested.askPasswordReset(""))
                .verifyComplete();

        verify(userServiceMock, never()).list(any(PageRequest.class));
        verify(mailSenderPortMock, never()).send(any(), anyString(), any());
    }

    @Test
    void should_reset_password_successfully() {
        when(passwordCheckerMock.checkPasswordStrength(any(User.class)))
                .thenReturn(Mono.just(new PasswordEvaluation(true, 65d, "ok")));

        StepVerifier.create(tested.resetPassword("valid-token", "newPassword"))
                .verifyComplete();

        verify(keyValuePersistencePortMock).get(assertArg(actual ->
                Assertions.assertThat(actual).isEqualTo("security:reset-password:OXoqnFv14szsOMJZa2grsb0F_m5OzqbBDPQnVf8iVAM")));
        verify(keyValuePersistencePortMock).remove(assertArg(actual ->
                Assertions.assertThat(actual).isEqualTo("security:reset-password:OXoqnFv14szsOMJZa2grsb0F_m5OzqbBDPQnVf8iVAM")));
        verify(passwordCheckerMock).checkPasswordStrength(assertArg((User actual) ->
                Assertions.assertThat(actual.password()).isEqualTo("newPassword")));
        verify(userServiceMock).update(assertArg(actual ->
                Assertions.assertThat(actual.self().password()).isEqualTo("newPassword")));
    }

    @Test
    void should_fail_reset_password_with_unsecure_password() {
        when(passwordCheckerMock.checkPasswordStrength(any(User.class)))
                .thenReturn(Mono.just(new PasswordEvaluation(false, 4d, "Fail")));

        StepVerifier.create(tested.resetPassword("valid-token", "newPassword"))
                .verifyError(PasswordEvaluationException.class);

        verify(keyValuePersistencePortMock).get(assertArg(actual ->
                Assertions.assertThat(actual).isEqualTo("security:reset-password:OXoqnFv14szsOMJZa2grsb0F_m5OzqbBDPQnVf8iVAM")));
        verify(keyValuePersistencePortMock, never()).remove(anyString());
        verify(passwordCheckerMock).checkPasswordStrength(assertArg((User actual) ->
                Assertions.assertThat(actual.password()).isEqualTo("newPassword")));
        verify(userServiceMock, never()).update(any());
    }
}