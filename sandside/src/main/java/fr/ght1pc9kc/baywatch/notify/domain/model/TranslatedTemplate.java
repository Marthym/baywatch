package fr.ght1pc9kc.baywatch.notify.domain.model;

import fr.ght1pc9kc.baywatch.notify.api.model.MailTemplateName;

public record TranslatedTemplate(
        MailTemplateName id,
        String subject,
        String body
) {
}
