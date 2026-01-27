package fr.ght1pc9kc.baywatch.notify.domain.ports;

import fr.ght1pc9kc.baywatch.notify.api.model.MailTemplateName;
import fr.ght1pc9kc.baywatch.notify.domain.model.MailTemplate;
import fr.ght1pc9kc.baywatch.notify.domain.model.TranslatedTemplate;
import reactor.core.publisher.Mono;

import java.util.Locale;

public interface MailTemplatePersistencePort {
    Mono<TranslatedTemplate> get(MailTemplateName name, Locale locale);
}
