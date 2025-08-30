package fr.ght1pc9kc.baywatch.notify.domain.ports;

import reactor.core.publisher.Mono;

public interface SmtpPort {
    Mono<Void> send(String to, String from, String subject, String body);
}
