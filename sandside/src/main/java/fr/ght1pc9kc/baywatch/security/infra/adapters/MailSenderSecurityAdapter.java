package fr.ght1pc9kc.baywatch.security.infra.adapters;

import fr.ght1pc9kc.baywatch.security.domain.ports.MailSenderPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class MailSenderSecurityAdapter implements MailSenderPort {
//    private final MailSender delegate;

    @Override
    public Mono<Void> send(MailTemplate template, String to) {
        return null;
    }
}
