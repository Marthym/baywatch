package fr.ght1pc9kc.baywatch.security.infra.adapters;

import fr.ght1pc9kc.baywatch.notify.api.MailClient;
import fr.ght1pc9kc.baywatch.security.domain.ports.MailSenderPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import static fr.ght1pc9kc.baywatch.notify.api.model.MailTemplateName.PASSWORD_RESET;

@Component
@RequiredArgsConstructor
public class MailSenderSecurityAdapter implements MailSenderPort {
    private final MailClient delegate;

    @Override
    public Mono<Void> send(MailTemplateType template, String to) {
        final var notifyTemplate = switch (template) {
            case PASSWORD_RESET -> PASSWORD_RESET;
        };
        return delegate.send(notifyTemplate, to);
    }
}
