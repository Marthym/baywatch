package fr.ght1pc9kc.baywatch.notify.domain.services;

import fr.ght1pc9kc.baywatch.notify.api.model.MailTemplateName;
import fr.ght1pc9kc.baywatch.notify.domain.MailTemplateService;
import fr.ght1pc9kc.baywatch.notify.domain.model.MailTemplate;
import fr.ght1pc9kc.baywatch.notify.domain.ports.MailTemplatePersistencePort;
import fr.ght1pc9kc.entity.api.Entity;
import fr.ght1pc9kc.juery.api.Criteria;
import fr.ght1pc9kc.juery.api.PageRequest;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

import java.util.Locale;

import static fr.ght1pc9kc.baywatch.notify.domain.model.NotifyConstants.MATE_PROP_LOCALE;
import static fr.ght1pc9kc.baywatch.notify.domain.model.NotifyConstants.MATE_PROP_NAME;

@RequiredArgsConstructor
public class MailTemplateServiceImpl implements MailTemplateService {
    private final MailTemplatePersistencePort persistencePort;

    @Override
    public Mono<Entity<MailTemplate>> get(MailTemplateName name, Locale locale) {
        PageRequest pageRequest = PageRequest.one(Criteria.property(MATE_PROP_NAME).eq(name.name())
                .and(Criteria.property(MATE_PROP_LOCALE).eq(locale.toLanguageTag())));
        return persistencePort.list(pageRequest).next();
    }
}
