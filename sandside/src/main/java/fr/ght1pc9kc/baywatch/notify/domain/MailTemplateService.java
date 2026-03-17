package fr.ght1pc9kc.baywatch.notify.domain;

import fr.ght1pc9kc.baywatch.notify.api.model.MailTemplateName;
import fr.ght1pc9kc.baywatch.notify.domain.model.TranslatedTemplate;
import reactor.core.publisher.Mono;

import java.util.Locale;

public interface MailTemplateService {
    Mono<TranslatedTemplate> get(MailTemplateName name, Locale locale);
}
