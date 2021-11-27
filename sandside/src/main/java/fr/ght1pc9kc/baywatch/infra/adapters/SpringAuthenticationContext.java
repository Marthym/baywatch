package fr.ght1pc9kc.baywatch.infra.adapters;

import fr.ght1pc9kc.baywatch.api.security.model.Role;
import fr.ght1pc9kc.baywatch.api.security.model.User;
import fr.ght1pc9kc.baywatch.domain.ports.AuthenticationFacade;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.util.context.Context;

/**
 * Implementation of the {@link AuthenticationFacade} using Spring context.
 * It permits to other modules to obtain the current authenticated user.
 */
@Service
@RequiredArgsConstructor
public class SpringAuthenticationContext implements AuthenticationFacade {
    @Override
    public Mono<User> getConnectedUser() {
        return ReactiveSecurityContextHolder.getContext()
                .map(SecurityContext::getAuthentication)
                .map(a -> (User) a.getPrincipal());
    }

    @Override
    public Context withAuthentication(User user) {
        Authentication authentication = new PreAuthenticatedAuthenticationToken(user, null,
                AuthorityUtils.createAuthorityList(user.role.name()));
        return ReactiveSecurityContextHolder.withAuthentication(authentication);
    }

    @Override
    public Context withSystemAuthentication() {
        User principal = User.builder()
                .id(Role.SYSTEM.name())
                .name(Role.SYSTEM.name())
                .login(Role.SYSTEM.name().toLowerCase())
                .role(Role.SYSTEM).build();
        Authentication authentication = new PreAuthenticatedAuthenticationToken(principal, null,
                AuthorityUtils.createAuthorityList(Role.SYSTEM.name()));
        return ReactiveSecurityContextHolder.withAuthentication(authentication);
    }
}
