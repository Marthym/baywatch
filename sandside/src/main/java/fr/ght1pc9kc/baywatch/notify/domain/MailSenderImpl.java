package fr.ght1pc9kc.baywatch.notify.domain;

import fr.ght1pc9kc.baywatch.notify.api.MailSender;
import fr.ght1pc9kc.baywatch.notify.api.model.MailTemplate;
import fr.ght1pc9kc.baywatch.notify.domain.ports.SmtpPort;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
public class MailSenderImpl implements MailSender {
    private final SmtpPort smtpPort;

    @Override
    public Mono<Void> send(MailTemplate template, String to) {
        return null;
    }
}
