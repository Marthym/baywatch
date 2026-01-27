package fr.ght1pc9kc.baywatch.security.domain.ports;

import fr.ght1pc9kc.baywatch.common.api.model.TemplateVariable;
import reactor.core.publisher.Mono;

import java.util.EnumMap;

public interface MailSenderPort {
    enum MailTemplateType {PASSWORD_RESET, WELCOME_USER}

    Mono<Void> send(MailTemplateType template, String to, EnumMap<TemplateVariable, String> variables);
}
