package fr.ght1pc9kc.baywatch.notify.infra.persistence;

import fr.ght1pc9kc.baywatch.notify.api.model.MailTemplateName;
import org.junit.jupiter.api.Test;
import reactor.core.scheduler.Schedulers;
import reactor.test.StepVerifier;

import java.util.Locale;

import static org.assertj.core.api.Assertions.assertThat;

class MailTemplatePersistenceAdapterTest {

    private final MailTemplatePersistenceAdapter tested =
            new MailTemplatePersistenceAdapter(Schedulers.immediate());

    @Test
    void should_load_template_from_classpath() {
        StepVerifier.create(tested.get(MailTemplateName.PASSWORD_RESET, Locale.ENGLISH))
                .assertNext(template -> {
                    assertThat(template.id()).isEqualTo(MailTemplateName.PASSWORD_RESET);
                    assertThat(template.subject()).isEqualTo("Baywatch - Request for password reset");
                    assertThat(template.body())
                            .contains("password reset")
                            .contains("${token}");
                })
                .verifyComplete();
    }

    @Test
    void should_error_when_template_not_supported() {
        StepVerifier.create(tested.get(MailTemplateName.WELCOME_USER, Locale.GERMAN))
                .verifyErrorSatisfies(ex -> {
                    assertThat(ex).isInstanceOf(IllegalArgumentException.class);
                    assertThat(ex.getMessage()).contains("Mail template not found");
                });
    }
}
