package fr.ght1pc9kc.baywatch.security.infra.adapters;

import fr.ght1pc9kc.baywatch.common.api.model.TemplateVariable;
import fr.ght1pc9kc.baywatch.notify.api.MailClient;
import fr.ght1pc9kc.baywatch.security.domain.ports.MailSenderPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.EnumMap;

import static fr.ght1pc9kc.baywatch.notify.api.model.MailTemplateName.PASSWORD_RESET;
import static fr.ght1pc9kc.baywatch.notify.api.model.MailTemplateName.WELCOME_USER;

@Component
@RequiredArgsConstructor
public class MailSenderSecurityAdapter implements MailSenderPort {
    private final MailClient delegate;

    @Override
    public Mono<Void> send(MailTemplateType template, String to, EnumMap<TemplateVariable, String> variables) {
        final var notifyTemplate = switch (template) {
            case PASSWORD_RESET -> PASSWORD_RESET;
            case WELCOME_USER -> WELCOME_USER;
        };
        return delegate.send(notifyTemplate, to, variables);
    }
}
