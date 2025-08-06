package fr.ght1pc9kc.baywatch.notify.domain.services;

import fr.ght1pc9kc.baywatch.notify.api.MailClient;
import fr.ght1pc9kc.baywatch.notify.api.model.MailTemplateName;
import fr.ght1pc9kc.baywatch.notify.domain.ports.MailQueuePersistencePort;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
public class MailClientImpl implements MailClient {
    private final MailQueuePersistencePort queuePersistencePort;

    @Override
    public Mono<Void> send(MailTemplateName template, String to) {
        return null;
    }
}
