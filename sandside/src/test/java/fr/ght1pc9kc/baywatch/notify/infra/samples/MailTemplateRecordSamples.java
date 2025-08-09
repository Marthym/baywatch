package fr.ght1pc9kc.baywatch.notify.infra.samples;

import fr.ght1pc9kc.baywatch.dsl.tables.records.MailTemplatesRecord;
import fr.ght1pc9kc.testy.jooq.model.RelationalDataSet;

import java.util.List;

import static fr.ght1pc9kc.baywatch.dsl.tables.MailTemplates.MAIL_TEMPLATES;

public class MailTemplateRecordSamples implements RelationalDataSet<MailTemplatesRecord> {
    public static final MailTemplateRecordSamples SAMPLE = new MailTemplateRecordSamples();

    public static final MailTemplatesRecord RESET_PASSWORD_FR_TEMPLATE_RECORD = MAIL_TEMPLATES.newRecord()
            .setMateId("TP01K273J8K1WATTWQZRRMDNR1CR")
            .setMateName("PASSWORD_RESET")
            .setMateLang("fr-FR")
            .setMateSubject("Réinitialisation de mot de passe")
            .setMateBody("""
                    Salut, <br/><br/>Vous avez demander un reset de mot de passe.
                    Clickez sur les lien suivant pour  <br/><br/>
                    <a href="https://${baseUrl}/reset-password?token=${token}">réinitialiser votre mot de passe</a><br/><br/>
                    Merci,<br/><br/>Baywatch
                    """);
    public static final MailTemplatesRecord RESET_PASSWORD_EN_TEMPLATE_RECORD = MAIL_TEMPLATES.newRecord()
            .setMateId("TP01K273J8ZXAA7YNYRENA00TJ1P")
            .setMateName("PASSWORD_RESET")
            .setMateLang("en-US")
            .setMateSubject("Reset password")
            .setMateBody("""
                    Hello, <br/><br/>You have requested a password reset.
                    Please use the following link to reset your password: <br/><br/>
                    <a href="https://${baseUrl}/reset-password?token=${token}">Reset password</a><br/><br/>
                    Thank you,<br/><br/>Baywatch
                    """);

    @Override
    public List<MailTemplatesRecord> records() {
        return List.of(
                RESET_PASSWORD_FR_TEMPLATE_RECORD,
                RESET_PASSWORD_EN_TEMPLATE_RECORD
        );
    }
}
