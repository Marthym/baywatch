package fr.ght1pc9kc.baywatch.security.api;

import fr.ght1pc9kc.baywatch.common.api.model.Entity;
import fr.ght1pc9kc.baywatch.security.api.model.Role;
import fr.ght1pc9kc.baywatch.security.api.model.User;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;
import reactor.core.publisher.Mono;
import reactor.util.context.Context;

import java.time.Instant;

/**
 * Facade used to obtain the connected user.
 */
public interface AuthenticationFacade {

    /**
     * Obtain the connected user.
     *
     * @return The connected user.
     */
    Mono<Entity<User>> getConnectedUser();

    /**
     * Update the {@link org.reactivestreams.Publisher} context with a specific authentication
     *
     * @param user The {@link User} as Principal of the Authentication
     * @return The authenticated Context
     */
    Context withAuthentication(Entity<User> user);

    static Context withSystemAuthentication() {
        Entity<User> principal = new Entity<>(Role.SYSTEM.name(), Entity.NO_ONE, Instant.EPOCH, User.builder()
                .name(Role.SYSTEM.name())
                .login(Role.SYSTEM.name().toLowerCase())
                .roles(Role.SYSTEM).build());
        Authentication authentication = new PreAuthenticatedAuthenticationToken(principal, null,
                AuthorityUtils.createAuthorityList(Role.SYSTEM.name()));
        return ReactiveSecurityContextHolder.withAuthentication(authentication);
    }
}
