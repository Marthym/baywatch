package fr.ght1pc9kc.baywatch.notify.domain.ports;

import fr.ght1pc9kc.baywatch.notify.domain.model.MailTemplate;
import fr.ght1pc9kc.entity.api.Entity;
import fr.ght1pc9kc.juery.api.PageRequest;
import reactor.core.publisher.Flux;

public interface MailTemplatePersistencePort {
    Flux<Entity<MailTemplate>> list(PageRequest pageRequest);
}
