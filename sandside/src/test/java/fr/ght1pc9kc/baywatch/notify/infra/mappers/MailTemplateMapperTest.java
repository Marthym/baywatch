package fr.ght1pc9kc.baywatch.notify.infra.mappers;

import fr.ght1pc9kc.baywatch.notify.api.model.MailTemplateName;
import fr.ght1pc9kc.baywatch.notify.domain.model.MailTemplate;
import fr.ght1pc9kc.entity.api.Entity;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.util.Locale;

import static fr.ght1pc9kc.baywatch.notify.infra.samples.MailTemplateRecordSamples.RESET_PASSWORD_EN_TEMPLATE_RECORD;

class MailTemplateMapperTest {

    private final MailTemplateMapper tested = Mappers.getMapper(MailTemplateMapper.class);

    @Test
    void should_map_record_to_mail_template() {
        Entity<MailTemplate> actual = tested.toEntity(RESET_PASSWORD_EN_TEMPLATE_RECORD);
        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(actual).isNotNull();
            softly.assertThat(actual.id()).isEqualTo("TP01K273J8ZXAA7YNYRENA00TJ1P");
            softly.assertThat(actual.self().name()).isEqualTo(MailTemplateName.PASSWORD_RESET);
            softly.assertThat(actual.self().locale()).isEqualTo(Locale.US);
            softly.assertThat(actual.self().subject()).isEqualTo("Reset password");
            softly.assertThat(actual.self().body()).isEqualTo("""
                    Hello, <br/><br/>You have requested a password reset.
                    Please use the following link to reset your password: <br/><br/>
                    <a href="https://${baseUrl}/reset-password?token=${token}">Reset password</a><br/><br/>
                    Thank you,<br/><br/>Baywatch
                    """);
        });
    }
}