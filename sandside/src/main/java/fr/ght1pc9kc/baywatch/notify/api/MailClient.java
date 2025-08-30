package fr.ght1pc9kc.baywatch.notify.api;

import fr.ght1pc9kc.baywatch.common.api.model.TemplateVariable;
import fr.ght1pc9kc.baywatch.notify.api.model.MailTemplateName;
import reactor.core.publisher.Mono;

import java.util.EnumMap;

public interface MailClient {
    Mono<Void> send(MailTemplateName template, String to, EnumMap<TemplateVariable, String> variables);
}
