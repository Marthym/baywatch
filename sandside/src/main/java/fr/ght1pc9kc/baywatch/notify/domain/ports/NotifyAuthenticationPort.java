package fr.ght1pc9kc.baywatch.notify.domain.ports;

import fr.ght1pc9kc.baywatch.common.api.model.TemplateVariable;
import fr.ght1pc9kc.entity.api.Entity;
import reactor.core.publisher.Mono;

import java.util.EnumMap;

public interface NotifyAuthenticationPort {
    Mono<Entity<EnumMap<TemplateVariable, String>>> getConnectedUser();
}
