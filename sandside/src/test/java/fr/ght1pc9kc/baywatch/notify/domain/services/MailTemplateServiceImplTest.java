package fr.ght1pc9kc.baywatch.notify.domain.services;

import fr.ght1pc9kc.baywatch.notify.api.model.MailTemplateName;
import fr.ght1pc9kc.baywatch.notify.domain.MailTemplateService;
import fr.ght1pc9kc.baywatch.notify.domain.model.TranslatedTemplate;
import fr.ght1pc9kc.baywatch.notify.domain.ports.MailTemplatePersistencePort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.Locale;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class MailTemplateServiceImplTest {

    private MailTemplatePersistencePort mockPersistencePort;
    private MailTemplateService tested;

    @BeforeEach
    void setUp() {
        mockPersistencePort = mock(MailTemplatePersistencePort.class);
        tested = new MailTemplateServiceImpl(mockPersistencePort);
    }

    @Test
    void should_get_template_from_persistence_port() {
        TranslatedTemplate expected = new TranslatedTemplate(
                MailTemplateName.PASSWORD_RESET,
                "Baywatch - Reset your password",
                "Hello ${username}, reset your password with ${token}"
        );

        when(mockPersistencePort.get(MailTemplateName.PASSWORD_RESET, Locale.ENGLISH))
                .thenReturn(Mono.just(expected));

        StepVerifier.create(tested.get(MailTemplateName.PASSWORD_RESET, Locale.ENGLISH))
                .assertNext(actual -> assertThat(actual).isEqualTo(expected))
                .verifyComplete();

        verify(mockPersistencePort).get(MailTemplateName.PASSWORD_RESET, Locale.ENGLISH);
    }

    @Test
    void should_propagate_persistence_port_error() {
        when(mockPersistencePort.get(MailTemplateName.PASSWORD_RESET, Locale.ENGLISH))
                .thenReturn(Mono.error(new IllegalArgumentException("Mail template not found")));

        StepVerifier.create(tested.get(MailTemplateName.PASSWORD_RESET, Locale.ENGLISH))
                .verifyErrorSatisfies(ex -> {
                    assertThat(ex).isInstanceOf(IllegalArgumentException.class);
                    assertThat(ex.getMessage()).contains("Mail template not found");
                });

        verify(mockPersistencePort).get(MailTemplateName.PASSWORD_RESET, Locale.ENGLISH);
    }
}
