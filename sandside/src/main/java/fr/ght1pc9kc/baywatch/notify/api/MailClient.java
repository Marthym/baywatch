package fr.ght1pc9kc.baywatch.notify.api;

import fr.ght1pc9kc.baywatch.notify.api.model.MailTemplateName;
import reactor.core.publisher.Mono;

public interface MailClient {
    Mono<Void> send(MailTemplateName template, String to);
}
