package fr.ght1pc9kc.baywatch.notify.infra.adapters;

import fr.ght1pc9kc.baywatch.common.api.model.TemplateVariable;
import fr.ght1pc9kc.baywatch.notify.domain.ports.NotifyAuthenticationPort;
import fr.ght1pc9kc.baywatch.security.api.AuthenticationFacade;
import fr.ght1pc9kc.entity.api.Entity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.EnumMap;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class NotifyAuthenticationAdapter implements NotifyAuthenticationPort {
    private final AuthenticationFacade authFacade;

    @Override
    public Mono<Entity<EnumMap<TemplateVariable, String>>> getConnectedUser() {
        return authFacade.getConnectedUser().map(e -> e.convert(user ->
                new EnumMap<>(Map.of(
                        TemplateVariable.LOGIN, user.login(),
                        TemplateVariable.USERNAME, user.name()
                ))));
    }
}
