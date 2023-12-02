package fr.ght1pc9kc.baywatch.teams.infra.adapters;

import fr.ght1pc9kc.baywatch.security.api.AuthenticationFacade;
import fr.ght1pc9kc.baywatch.security.api.AuthorizationService;
import fr.ght1pc9kc.baywatch.security.api.model.Permission;
import fr.ght1pc9kc.baywatch.teams.domain.ports.TeamAuthFacade;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Delegate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TeamAuthFacadeAdapter implements TeamAuthFacade {
    private final @Delegate AuthenticationFacade delegate;
    private final AuthorizationService authorizationService;

    @Override
    public Mono<Void> grantAuthorization(String userId, Collection<String> permissions) {
        try {
            Set<Permission> perms = permissions.stream().map(Permission::from).collect(Collectors.toUnmodifiableSet());
            return authorizationService.grants(userId, perms).then();
        } catch (Exception e) {
            return Mono.error(() -> new IllegalArgumentException("Unable to grant permissions : " + permissions, e));
        }
    }

    @Override
    public Mono<Void> revokeAuthorization(String permission, Collection<String> userIds) {
        try {
            return authorizationService.revokes(Permission.from(permission), userIds).then();
        } catch (Exception e) {
            return Mono.error(() -> new IllegalArgumentException("Unable to revoke authorization " + permission, e));
        }
    }

    @Override
    public Mono<Void> removeAuthorizations(Collection<String> authorizations) {
        try {
            Set<Permission> perms = authorizations.stream().map(Permission::from).collect(Collectors.toUnmodifiableSet());
            return this.getConnectedUser().flatMap(user -> authorizationService.remove(perms)).then();
        } catch (Exception e) {
            return Mono.error(() -> new IllegalArgumentException("Unable to remove authorization " + authorizations, e));
        }
    }

    @Override
    public Flux<String> listManagers(String teamId) {
        return authorizationService.listGrantedUsers(Permission.manager(teamId));
    }
}
