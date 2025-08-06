package fr.ght1pc9kc.baywatch.security.domain.ports;

import reactor.core.publisher.Mono;

public interface MailSenderPort {
    enum MailTemplateType {PASSWORD_RESET}

    Mono<Void> send(MailTemplateType template, String to);
}
