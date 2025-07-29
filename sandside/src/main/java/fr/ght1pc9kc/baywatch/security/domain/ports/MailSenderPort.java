package fr.ght1pc9kc.baywatch.security.domain.ports;

import reactor.core.publisher.Mono;

public interface MailSenderPort {
    enum MailTemplate {PASSWORD_RESET}

    Mono<Void> send(MailTemplate template, String to);
}
