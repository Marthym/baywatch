package fr.ght1pc9kc.baywatch.infra.adapters;

import fr.ght1pc9kc.baywatch.api.model.User;
import fr.ght1pc9kc.baywatch.domain.ports.AuthenticationFacade;
import fr.ght1pc9kc.baywatch.infra.mappers.BaywatchMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

/**
 * Implementation of the {@link AuthenticationFacade} using Spring context.
 * It permits to other modules to obtain the current authenticated user.
 */
@Service
@RequiredArgsConstructor
public class SpringAuthenticationContext implements AuthenticationFacade {
    private final BaywatchMapper baywatchMapper;

    @Override
    public Mono<User> getConnectedUser() {
        return ReactiveSecurityContextHolder.getContext()
                .map(SecurityContext::getAuthentication)
                .map(baywatchMapper::principalToUser);
    }
}
