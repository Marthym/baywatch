package fr.ght1pc9kc.baywatch.scraper.infra.adapters;

import fr.ght1pc9kc.baywatch.scraper.domain.ports.ScrapingAuthentFacade;
import fr.ght1pc9kc.baywatch.security.api.AuthenticationFacade;
import fr.ght1pc9kc.baywatch.security.api.model.Role;
import fr.ght1pc9kc.baywatch.security.api.model.RoleUtils;
import fr.ght1pc9kc.baywatch.security.api.model.User;
import fr.ght1pc9kc.entity.api.Entity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import reactor.util.context.Context;

@Component
@RequiredArgsConstructor
public class ScrapingAuthentFacadeAdapter implements ScrapingAuthentFacade {
    private final AuthenticationFacade authenticationFacade;

    @Override
    public Mono<Entity<User>> getConnectedUser() {
        return authenticationFacade.getConnectedUser();
    }

    @Override
    public Context withSystemAuthentication() {
        return AuthenticationFacade.withSystemAuthentication();
    }

    @Override
    public boolean hasSystemRole(Entity<User> current) {
        return RoleUtils.hasRole(current.self(), Role.SYSTEM);
    }
}
