package fr.ght1pc9kc.baywatch.notify.api;

import fr.ght1pc9kc.baywatch.notify.api.model.MailTemplate;
import reactor.core.publisher.Mono;

public interface MailSender {
    Mono<Void> send(MailTemplate template, String to);
}
