package fr.ght1pc9kc.baywatch.admin.infra.controllers;

import fr.ght1pc9kc.baywatch.admin.api.AppConfigurationService;
import fr.ght1pc9kc.baywatch.admin.api.model.ParameterSet;
import fr.ght1pc9kc.baywatch.admin.infra.exceptions.InvalidConfigurationException;
import fr.ght1pc9kc.baywatch.admin.infra.model.MailSmtpConfigurationForm;
import fr.ght1pc9kc.testy.core.extensions.WithJsonMapper;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Path;
import org.assertj.core.api.Assertions;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.mockito.ArgumentCaptor;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import tools.jackson.databind.ObjectMapper;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

class AppConfigurationControllerTest {
    @RegisterExtension
    private static final WithJsonMapper wMapper = WithJsonMapper.builder().build();

    private AppConfigurationController tested;

    private AppConfigurationService mockService;

    @BeforeEach
    void setUp(ObjectMapper objectMapper) {
        mockService = mock(AppConfigurationService.class);
        tested = new AppConfigurationController(mockService, objectMapper);
    }

    @Test
    void should_get_mail_smtp_configuration() {
        // Given
        ParameterSet ps = mock(ParameterSet.class);
        Map<String, Object> expected = Map.of(
                "mail", Map.of("smtp", Map.of("host", "smtp.example.test"))
        );

        when(mockService.get("mail.smtp")).thenReturn(Mono.just(ps));
        when(ps.toMap()).thenReturn(expected);

        // When
        Mono<Map<String, Object>> result = tested.adminAppConfigurationMailSmtp();

        // Then
        StepVerifier.create(result)
                .assertNext(actual -> Assertions.assertThat(actual).isEqualTo(expected))
                .verifyComplete();

        verify(mockService).get("mail.smtp");
        verifyNoMoreInteractions(mockService);
    }

    @Test
    @SuppressWarnings("unchecked")
    void should_update_mail_smtp_configuration_and_flatten_with_prefix() {
        MailSmtpConfigurationForm form = new MailSmtpConfigurationForm(
                "smtp.example.test",
                587,
                true,
                "obiwan",
                "secret",
                true,
                new MailSmtpConfigurationForm.Ssl("TLSv1.3", true),
                "obiwan@jedi.com",
                60
        );

        ParameterSet ps = mock(ParameterSet.class);
        Map<String, Object> expected = Map.of("ok", true);

        when(mockService.update(any())).thenReturn(Mono.just(ps));
        when(ps.toMap()).thenReturn(expected);

        Mono<Map<String, Object>> result = tested.adminAppConfigurationMailSmtpUpdate(form);

        StepVerifier.create(result)
                .assertNext(actual -> Assertions.assertThat(actual).isEqualTo(expected))
                .verifyComplete();

        ArgumentCaptor<List<Map.Entry<String, String>>> captor = ArgumentCaptor.forClass(List.class);

        verify(mockService).update(captor.capture());

        List<Map.Entry<String, String>> flattened = captor.getValue();

        SoftAssertions.assertSoftly(softly -> softly.assertThat(flattened).contains(
                Map.entry("mail.smtp.host", "smtp.example.test"),
                Map.entry("mail.smtp.port", "587"),
                Map.entry("mail.smtp.secure", "true"),
                Map.entry("mail.smtp.username", "obiwan"),
                Map.entry("mail.smtp.password", "secret"),
                Map.entry("mail.smtp.requireTls", "true"),
                Map.entry("mail.smtp.ssl.protocols", "TLSv1.3"),
                Map.entry("mail.smtp.ssl.checkserveridentity", "true"),
                Map.entry("mail.smtp.from", "obiwan@jedi.com"),
                Map.entry("mail.smtp.pollingIntervalSeconds", "60")
        ));

        verifyNoMoreInteractions(mockService);
    }

    @Test
    void should_map_constraint_violation_exception_to_invalid_configuration_exception_with_field() {
        MailSmtpConfigurationForm form = new MailSmtpConfigurationForm(
                "smtp.example.test",
                587,
                true,
                "obiwan",
                "secret",
                true,
                new MailSmtpConfigurationForm.Ssl("TLSv1.3", true),
                "obiwan@jedi.com",
                60
        );

        ConstraintViolation<?> violation = mock(ConstraintViolation.class);
        Path path = mock(Path.class);

        when(violation.getMessage()).thenReturn("must not be blank");
        when(violation.getPropertyPath()).thenReturn(path);
        when(path.toString()).thenReturn("mailSmtp.host");

        ConstraintViolationException cve = new ConstraintViolationException("validation failed", Set.of(violation));

        when(mockService.update(any())).thenReturn(Mono.error(cve));

        Mono<Map<String, Object>> result = tested.adminAppConfigurationMailSmtpUpdate(form);

        StepVerifier.create(result).verifyErrorSatisfies(ex -> {
            Assertions.assertThat(ex).isInstanceOf(InvalidConfigurationException.class);
            InvalidConfigurationException ice = (InvalidConfigurationException) ex;
            Assertions.assertThat(ice.getLocalizedMessage()).isEqualTo("must not be blank");
        });

        verify(mockService).update(any());
    }

    @Test
    void should_map_constraint_violation_exception_without_violations_to_invalid_configuration_exception_with_empty_properties() {
        // Given
        MailSmtpConfigurationForm form = new MailSmtpConfigurationForm(
                "smtp.example.test",
                587,
                true,
                "obiwan",
                "secret",
                true,
                new MailSmtpConfigurationForm.Ssl("TLSv1.3", true),
                "obiwan@jedi.com",
                60
        );

        ConstraintViolationException cve = new ConstraintViolationException("validation failed", Set.of());

        when(mockService.update(any())).thenReturn(Mono.error(cve));

        // When
        Mono<Map<String, Object>> result = tested.adminAppConfigurationMailSmtpUpdate(form);

        // Then
        StepVerifier.create(result)
                .verifyErrorSatisfies(ex -> {
                    Assertions.assertThat(ex).isInstanceOf(InvalidConfigurationException.class);
                    Assertions.assertThat(ex.getLocalizedMessage()).isEqualTo("validation failed");
                });

        verify(mockService).update(any());
    }
}