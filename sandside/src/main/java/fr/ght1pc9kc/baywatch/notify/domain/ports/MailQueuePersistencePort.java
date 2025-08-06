package fr.ght1pc9kc.baywatch.notify.domain.ports;

import fr.ght1pc9kc.baywatch.notify.domain.model.Mail;
import fr.ght1pc9kc.entity.api.Entity;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface MailQueuePersistencePort {
    Mono<Void> push(Entity<Mail> mail);
    Flux<Entity<Mail>> consume();
}
