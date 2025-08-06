package fr.ght1pc9kc.baywatch.notify.domain.ports;

import fr.ght1pc9kc.baywatch.notify.domain.model.MailContextVariables;
import fr.ght1pc9kc.entity.api.Entity;
import reactor.core.publisher.Mono;

public interface NotifyAuthenticationPort {
    Mono<Entity<MailContextVariables>> getConnectedUser();
}
