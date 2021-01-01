package fr.ght1pc9kc.baywatch.infra.adapters;

import fr.ght1pc9kc.baywatch.domain.ports.AuthenticationFacade;
import fr.ght1pc9kc.baywatch.api.model.User;
import lombok.RequiredArgsConstructor;
import org.springframework.core.convert.ConversionService;
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
@SuppressWarnings("ReactiveStreamsNullableInLambdaInTransform")
public class SpringAuthenticationContext implements AuthenticationFacade {
    private final ConversionService conversionService;

    @Override
    public Mono<User> getConnectedUser() {
        return ReactiveSecurityContextHolder.getContext()
                .map(SecurityContext::getAuthentication)
                .map(authent -> conversionService.convert(authent.getPrincipal(), User.class));
    }
}
