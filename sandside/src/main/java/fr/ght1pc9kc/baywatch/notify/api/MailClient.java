package fr.ght1pc9kc.baywatch.notify.api;

import fr.ght1pc9kc.baywatch.notify.api.model.MailTemplateName;
import fr.ght1pc9kc.baywatch.notify.domain.model.MailContextVariables;
import reactor.core.publisher.Mono;

public interface MailClient {
    Mono<Void> send(MailTemplateName template, String to, MailContextVariables variables);
}
