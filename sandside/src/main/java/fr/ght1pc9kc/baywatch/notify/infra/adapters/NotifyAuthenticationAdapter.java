package fr.ght1pc9kc.baywatch.notify.infra.adapters;

import fr.ght1pc9kc.baywatch.notify.domain.model.MailContextVariables;
import fr.ght1pc9kc.baywatch.notify.domain.ports.NotifyAuthenticationPort;
import fr.ght1pc9kc.baywatch.security.api.AuthenticationFacade;
import fr.ght1pc9kc.entity.api.Entity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class NotifyAuthenticationAdapter implements NotifyAuthenticationPort {
    private final AuthenticationFacade authFacade;

    @Override
    public Mono<Entity<MailContextVariables>> getConnectedUser() {
        return authFacade.getConnectedUser().map(e -> e.convert(user ->
                MailContextVariables.builder()
                        .login(user.login())
                        .username(user.name())
                        .build()));
    }
}
