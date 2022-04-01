package fr.ght1pc9kc.baywatch.security.infra.adapters;

import fr.ght1pc9kc.baywatch.common.api.model.Entity;
import fr.ght1pc9kc.baywatch.security.api.AuthenticationFacade;
import fr.ght1pc9kc.baywatch.security.api.model.Role;
import fr.ght1pc9kc.baywatch.security.api.model.User;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.util.context.Context;

import java.time.Instant;

/**
 * Implementation of the {@link AuthenticationFacade} using Spring context.
 * It permits to other modules to obtain the current authenticated user.
 */
@Service
@RequiredArgsConstructor
public class SpringAuthenticationContext implements AuthenticationFacade {
    @Override
    @SuppressWarnings("unchecked")
    public Mono<Entity<User>> getConnectedUser() {
        return ReactiveSecurityContextHolder.getContext()
                .map(SecurityContext::getAuthentication)
                .map(a -> (Entity<User>) a.getPrincipal());
    }

    @Override
    public Context withAuthentication(Entity<User> user) {
        Authentication authentication = new PreAuthenticatedAuthenticationToken(user, null,
                AuthorityUtils.createAuthorityList(user.self.role.name()));
        return ReactiveSecurityContextHolder.withAuthentication(authentication);
    }

    @Override
    public Context withSystemAuthentication() {
        Entity<User> principal = new Entity<>(Role.SYSTEM.name(), Entity.NO_ONE, Instant.EPOCH, User.builder()
                .name(Role.SYSTEM.name())
                .login(Role.SYSTEM.name().toLowerCase())
                .role(Role.SYSTEM).build());
        Authentication authentication = new PreAuthenticatedAuthenticationToken(principal, null,
                AuthorityUtils.createAuthorityList(Role.SYSTEM.name()));
        return ReactiveSecurityContextHolder.withAuthentication(authentication);
    }
}
