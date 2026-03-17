package fr.ght1pc9kc.baywatch.notify.domain.services;

import fr.ght1pc9kc.baywatch.notify.api.model.MailTemplateName;
import fr.ght1pc9kc.baywatch.notify.domain.MailTemplateService;
import fr.ght1pc9kc.baywatch.notify.domain.model.TranslatedTemplate;
import fr.ght1pc9kc.baywatch.notify.domain.ports.MailTemplatePersistencePort;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

import java.util.Locale;

@RequiredArgsConstructor
public class MailTemplateServiceImpl implements MailTemplateService {
    private final MailTemplatePersistencePort persistencePort;

    @Override
    public Mono<TranslatedTemplate> get(MailTemplateName name, Locale locale) {
        return persistencePort.get(name, locale);
    }
}
