package fr.ght1pc9kc.baywatch.notify.domain.model;

import fr.ght1pc9kc.baywatch.notify.api.model.MailTemplateName;

import java.util.Locale;

public record MailTemplate(
        MailTemplateName name,
        Locale locale,
        String subject
) {
}
