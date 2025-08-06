package fr.ght1pc9kc.baywatch.notify.domain;

import fr.ght1pc9kc.baywatch.notify.domain.model.MailTemplate;
import fr.ght1pc9kc.entity.api.Entity;
import reactor.core.publisher.Mono;

import java.util.Locale;

public interface MailTemplateService {
    Mono<Entity<MailTemplate>> get(MailTemplate name, Locale locale);
}
