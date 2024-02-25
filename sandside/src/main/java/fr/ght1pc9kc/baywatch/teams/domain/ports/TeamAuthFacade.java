package fr.ght1pc9kc.baywatch.teams.domain.ports;

import fr.ght1pc9kc.baywatch.common.api.DefaultMeta;
import fr.ght1pc9kc.baywatch.security.api.model.Role;
import fr.ght1pc9kc.baywatch.security.api.model.User;
import fr.ght1pc9kc.entity.api.Entity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.context.Context;

import java.util.Collection;
import java.util.List;

public interface TeamAuthFacade {
    /**
     * Obtain the connected user.
     *
     * @return The connected user.
     */
    Mono<Entity<User>> getConnectedUser();

    /**
     * Grant a {@code MANAGER} permission for et team to the current user
     *
     * @param authorizations The permissions to grant
     * @param userId         The {@link User} Id to grant
     * @return {@code Void} when operation was finished
     * @throws IllegalArgumentException if authorization does not contain entity ID or if the role is unknown
     */
    Mono<Void> grantAuthorization(String userId, Collection<String> authorizations);

    /**
     * Revoke a {@code MANAGER} permission for et team to the current user
     *
     * @param permission The permission to revoke
     * @param userIds    The {@link User} Ids to revoke
     * @return {@code Void} when operation was finished
     * @throws IllegalArgumentException if authorization does not contain entity ID or if the role is unknown
     */
    Mono<Void> revokeAuthorization(String permission, Collection<String> userIds);

    /**
     * Remove permission from te database. Revoke for all users
     *
     * @param authorizations The permissions to remove
     * @return {@code Void} when operation was finished
     * @throws IllegalArgumentException if authorization does not contain entity ID or if the role is unknown
     */
    Mono<Void> removeAuthorizations(Collection<String> authorizations);

    /**
     * List the managers for the given {@link fr.ght1pc9kc.baywatch.teams.api.model.Team}
     *
     * @param teamId The team ID
     * @return List of the managers id for the given Team
     */
    Flux<String> listManagers(String teamId);

    static Context withSystemAuthentication(String userIdImpersonation) {
        Entity<User> principal = Entity.identify(User.builder()
                        .name("Team Domain")
                        .login(DefaultMeta.NO_ONE)
                        .roles(List.of(Role.SYSTEM)).build())
                .withId(userIdImpersonation);
        Authentication authentication = new PreAuthenticatedAuthenticationToken(principal, null,
                AuthorityUtils.createAuthorityList(Role.SYSTEM.name()));
        return ReactiveSecurityContextHolder.withAuthentication(authentication);
    }
}
