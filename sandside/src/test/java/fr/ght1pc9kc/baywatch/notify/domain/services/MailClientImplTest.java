
package fr.ght1pc9kc.baywatch.notify.domain.services;

import fr.ght1pc9kc.baywatch.common.api.model.TemplateVariable;
import fr.ght1pc9kc.baywatch.notify.api.MailClient;
import fr.ght1pc9kc.baywatch.notify.api.model.MailTemplateName;
import fr.ght1pc9kc.baywatch.notify.domain.MailTemplateService;
import fr.ght1pc9kc.baywatch.notify.domain.exceptions.MailLimitExceededException;
import fr.ght1pc9kc.baywatch.notify.domain.model.Mail;
import fr.ght1pc9kc.baywatch.notify.domain.model.MailMeta;
import fr.ght1pc9kc.baywatch.notify.domain.model.TranslatedTemplate;
import fr.ght1pc9kc.baywatch.notify.domain.ports.MailQueuePersistencePort;
import fr.ght1pc9kc.baywatch.notify.domain.ports.NotifyAuthenticationPort;
import fr.ght1pc9kc.baywatch.notify.domain.ports.NotifyClientInfoPort;
import fr.ght1pc9kc.entity.api.Entity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.URI;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.EnumMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import static fr.ght1pc9kc.baywatch.tests.samples.UserSamples.OBIWAN;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class MailClientImplTest {

    private MailClient tested;
    private MailTemplateService mockTemplateService;
    private NotifyAuthenticationPort mockAuthenticationPort;
    private NotifyClientInfoPort mockClientInfoPort;
    private MailQueuePersistencePort mockQueuePersistencePort;

    private final Clock fixedClock = Clock.fixed(Instant.parse("2024-05-04T12:00:00Z"), ZoneOffset.UTC);

    @BeforeEach
    void setUp() {
        mockTemplateService = mock(MailTemplateService.class);
        mockAuthenticationPort = mock(NotifyAuthenticationPort.class);
        mockClientInfoPort = mock(NotifyClientInfoPort.class);
        mockQueuePersistencePort = mock(MailQueuePersistencePort.class);

        setupDefaultMockBehaviors();

        Set<String> whitelistIps = Set.of("127.0.0.1", "192.168.1.100", "10.0.0.1");

        tested = new MailClientImpl(
                mockTemplateService,
                mockAuthenticationPort,
                mockClientInfoPort,
                mockQueuePersistencePort,
                whitelistIps, "Baywatch"
        );

        ((MailClientImpl) tested).setClock(fixedClock);
    }

    private void setupDefaultMockBehaviors() {
        when(mockAuthenticationPort.getConnectedUser())
                .thenReturn(Mono.just(Entity.identify(new EnumMap<>(Map.of(
                        TemplateVariable.LOGIN, OBIWAN.self().login(),
                        TemplateVariable.USERNAME, OBIWAN.self().name()
                ))).withId(OBIWAN.id())));

        when(mockClientInfoPort.getRemoteAddress())
                .thenReturn(Mono.just(new InetSocketAddress("127.0.0.1", 8080)));
        when(mockClientInfoPort.getLocale())
                .thenReturn(Mono.just(Locale.FRANCE));
        when(mockClientInfoPort.getAvailableLanguages())
                .thenReturn(List.of(Locale.FRANCE, Locale.US, Locale.GERMANY));
        when(mockClientInfoPort.getBaseUrl())
                .thenReturn(Mono.just(URI.create("https://jedi.temple")));

        TranslatedTemplate passwordResetTemplate = new TranslatedTemplate(MailTemplateName.PASSWORD_RESET,
                "Réinitialisation de mot de passe",
                "Bonjour ${username}, voici votre lien de réinitialisation : ${baseUrl}/reset?token=${token}");
        when(mockTemplateService.get(any(MailTemplateName.class), any(Locale.class)))
                .thenReturn(Mono.just(passwordResetTemplate));

        when(mockQueuePersistencePort.push(any()))
                .thenReturn(Mono.empty());
    }

    @Test
    @SuppressWarnings("unchecked")
    void should_send_mail_successfully() {
        MailTemplateName template = MailTemplateName.PASSWORD_RESET;
        String toEmail = "obi-wan@jedi.temple";
        EnumMap<TemplateVariable, String> variables = new EnumMap<>(Map.of(
                TemplateVariable.USERNAME, "Obi-Wan Kenobi",
                TemplateVariable.TOKEN, "secret-reset-token-123"
        ));

        StepVerifier.create(tested.send(template, toEmail, variables))
                .verifyComplete();

        ArgumentCaptor<Entity<Mail>> mailCaptor = ArgumentCaptor.forClass(Entity.class);
        verify(mockQueuePersistencePort).push(mailCaptor.capture());

        Entity<Mail> queuedMail = mailCaptor.getValue();
        assertThat(queuedMail.self().to()).isEqualTo(toEmail);
        assertThat(queuedMail.self().subject()).isEqualTo("Réinitialisation de mot de passe");
        assertThat(queuedMail.self().message())
                .contains("Obi-Wan Kenobi")
                .contains("https://jedi.temple/reset?token=secret-reset-token-123");
        assertThat(queuedMail.id()).startsWith("ML");
    }

    @Test
    void should_handle_whitelisted_ip_without_rate_limiting() {
        String toEmail = "unlimited@jedi.temple";
        EnumMap<TemplateVariable, String> variables = new EnumMap<>(Map.of(
                TemplateVariable.USERNAME, "Test User"
        ));

        for (int i = 0; i < 10; i++) {
            StepVerifier.create(tested.send(MailTemplateName.PASSWORD_RESET, toEmail, variables))
                    .verifyComplete();
        }

        verify(mockQueuePersistencePort, times(10)).push(any());
    }

    @Test
    void should_apply_rate_limiting_for_non_whitelisted_ip() throws Exception {
        InetAddress suspiciousAddress = InetAddress.getByName("192.168.1.200");
        when(mockClientInfoPort.getRemoteAddress())
                .thenReturn(Mono.just(new InetSocketAddress(suspiciousAddress, 8080)));

        String toEmail = "target@victim.com";
        EnumMap<TemplateVariable, String> variables = new EnumMap<>(Map.of(
                TemplateVariable.USERNAME, "Target User"
        ));

        // When - Send emails up to the limit
        for (int i = 0; i < 5; i++) {
            StepVerifier.create(tested.send(MailTemplateName.PASSWORD_RESET, toEmail, variables))
                    .verifyComplete();
        }

        // Then - 6th email should fail with rate limit exception
        StepVerifier.create(tested.send(MailTemplateName.PASSWORD_RESET, toEmail, variables))
                .verifyError(MailLimitExceededException.class);

        verify(mockQueuePersistencePort, times(5)).push(any());
    }

    @Test
    void should_fail_when_no_authenticated_user() {
        when(mockAuthenticationPort.getConnectedUser())
                .thenReturn(Mono.empty());

        EnumMap<TemplateVariable, String> variables = new EnumMap<>(TemplateVariable.class);
        StepVerifier.create(tested.send(MailTemplateName.PASSWORD_RESET, "test@mandalorian.force", variables))
                .verifyError(IllegalStateException.class);

        verify(mockQueuePersistencePort, never()).push(any());
    }

    @Test
    void should_select_best_matching_locale() {
        when(mockClientInfoPort.getLocale())
                .thenReturn(Mono.just(Locale.FRENCH));
        when(mockClientInfoPort.getAvailableLanguages())
                .thenReturn(List.of(Locale.US, Locale.GERMANY, Locale.FRANCE));

        EnumMap<TemplateVariable, String> variables = new EnumMap<>(TemplateVariable.class);
        StepVerifier.create(tested.send(MailTemplateName.PASSWORD_RESET, "test@mandalorian.force", variables))
                .verifyComplete();

        verify(mockTemplateService).get(MailTemplateName.PASSWORD_RESET, Locale.FRANCE);
    }

    @Test
    void should_fallback_to_first_available_language_when_no_match() {
        when(mockClientInfoPort.getLocale())
                .thenReturn(Mono.just(Locale.JAPANESE));
        when(mockClientInfoPort.getAvailableLanguages())
                .thenReturn(List.of(Locale.FRANCE, Locale.US, Locale.GERMANY));

        EnumMap<TemplateVariable, String> variables = new EnumMap<>(TemplateVariable.class);
        StepVerifier.create(tested.send(MailTemplateName.PASSWORD_RESET, "test@mandalorian.force", variables))
                .verifyComplete();

        verify(mockTemplateService).get(MailTemplateName.PASSWORD_RESET, Locale.FRANCE);
    }

    @Test
    @SuppressWarnings("unchecked")
    void should_use_default_base_url_when_none_available() {
        when(mockClientInfoPort.getBaseUrl())
                .thenReturn(Mono.empty());

        EnumMap<TemplateVariable, String> variables = new EnumMap<>(Map.of(
                TemplateVariable.TOKEN, "test-token"
        ));

        StepVerifier.create(tested.send(MailTemplateName.PASSWORD_RESET, "test@mandalorian.force", variables))
                .verifyComplete();

        ArgumentCaptor<Entity<Mail>> mailCaptor = ArgumentCaptor.forClass(Entity.class);
        verify(mockQueuePersistencePort).push(mailCaptor.capture());

        Entity<Mail> queuedMail = mailCaptor.getValue();
        assertThat(queuedMail.self().message()).contains("http://localhost/");
    }

    @Test
    @SuppressWarnings("unchecked")
    void should_interpolate_template_variables_correctly() {
        TranslatedTemplate customTemplate = new TranslatedTemplate(MailTemplateName.PASSWORD_RESET,
                "Welcome ${username}!",
                "Hello ${username}, welcome to ${baseUrl}! Your token is ${token} and custom var is ${customVar}.");
        when(mockTemplateService.get(any(MailTemplateName.class), any(Locale.class)))
                .thenReturn(Mono.just(customTemplate));

        EnumMap<TemplateVariable, String> variables = new EnumMap<>(Map.of(
                TemplateVariable.USERNAME, "Luke Skywalker",
                TemplateVariable.TOKEN, "jedi-token-456"
        ));

        StepVerifier.create(tested.send(MailTemplateName.PASSWORD_RESET, "luke@rebels.com", variables))
                .verifyComplete();

        ArgumentCaptor<Entity<Mail>> mailCaptor = ArgumentCaptor.forClass(Entity.class);
        verify(mockQueuePersistencePort).push(mailCaptor.capture());

        Entity<Mail> queuedMail = mailCaptor.getValue();
        assertThat(queuedMail.self().subject()).isEqualTo("Welcome Luke Skywalker!");
        assertThat(queuedMail.self().message())
                .contains("Hello Luke Skywalker")
                .contains("welcome to https://jedi.temple")
                .contains("Your token is jedi-token-456")
                .contains("custom var is ${customVar}");
    }

    @Test
    void should_handle_template_service_error() {
        when(mockTemplateService.get(any(MailTemplateName.class), any(Locale.class)))
                .thenReturn(Mono.error(new RuntimeException("Template not found")));

        EnumMap<TemplateVariable, String> variables = new EnumMap<>(TemplateVariable.class);
        StepVerifier.create(tested.send(MailTemplateName.PASSWORD_RESET, "test@mandalorian.force", variables))
                .verifyError(RuntimeException.class);

        verify(mockQueuePersistencePort, never()).push(any());
    }

    @Test
    void should_handle_queue_persistence_error() {
        when(mockQueuePersistencePort.push(any()))
                .thenReturn(Mono.error(new RuntimeException("Queue is full")));

        EnumMap<TemplateVariable, String> variables = new EnumMap<>(TemplateVariable.class);
        StepVerifier.create(tested.send(MailTemplateName.PASSWORD_RESET, "test@mandalorian.force", variables))
                .verifyError(RuntimeException.class);
    }

    @Test
    @SuppressWarnings("unchecked")
    void should_set_correct_mail_metadata() {
        EnumMap<TemplateVariable, String> variables = new EnumMap<>(Map.of(
                TemplateVariable.USERNAME, "Yoda"
        ));

        StepVerifier.create(tested.send(MailTemplateName.PASSWORD_RESET, "yoda@jedi.temple", variables))
                .verifyComplete();

        ArgumentCaptor<Entity<Mail>> mailCaptor = ArgumentCaptor.forClass(Entity.class);
        verify(mockQueuePersistencePort).push(mailCaptor.capture());

        Entity<Mail> queuedMail = mailCaptor.getValue();
        assertThat(queuedMail.meta(MailMeta.createdAt, Instant.class)).contains(fixedClock.instant());
        assertThat(queuedMail.meta(MailMeta.createdBy)).contains(OBIWAN.id());
        assertThat(queuedMail.meta(MailMeta.template)).contains("PASSWORD_RESET");
        assertThat(queuedMail.id()).startsWith("ML");
    }

    @Test
    void should_handle_different_mail_template_names() {
        MailTemplateName[] templates = MailTemplateName.values();
        String email = "test@mandalorian.force";
        EnumMap<TemplateVariable, String> variables = new EnumMap<>(TemplateVariable.class);

        for (MailTemplateName template : templates) {
            StepVerifier.create(tested.send(template, email, variables))
                    .verifyComplete();
        }

        verify(mockQueuePersistencePort, times(templates.length)).push(any());
    }

    @Test
    void should_handle_rate_limiting_per_ip_and_email_combination() throws Exception {
        // Given - Same IP but different target emails
        InetAddress testAddress = InetAddress.getByName("192.168.1.87");
        when(mockClientInfoPort.getRemoteAddress())
                .thenReturn(Mono.just(new InetSocketAddress(testAddress, 8080)));

        EnumMap<TemplateVariable, String> variables = new EnumMap<>(TemplateVariable.class);

        // When - Send to different emails should each have their own counter
        for (int i = 0; i < 5; i++) {
            StepVerifier.create(tested.send(MailTemplateName.PASSWORD_RESET, "r2d2@republic.com", variables))
                    .verifyComplete();
            StepVerifier.create(tested.send(MailTemplateName.PASSWORD_RESET, "c3po@republic.com", variables))
                    .verifyComplete();
        }

        // Then - Should succeed for both emails (separate counters)
        verify(mockQueuePersistencePort, times(10)).push(any());

        // But 6th email to same address should fail
        StepVerifier.create(tested.send(MailTemplateName.PASSWORD_RESET, "r2d2@republic.com", variables))
                .verifyError(MailLimitExceededException.class);
    }
}