package fr.ght1pc9kc.baywatch.teams.infra.adapters;

import fr.ght1pc9kc.baywatch.security.api.AuthenticationFacade;
import fr.ght1pc9kc.baywatch.security.api.UserService;
import fr.ght1pc9kc.baywatch.security.api.model.Permission;
import fr.ght1pc9kc.baywatch.teams.domain.ports.TeamAuthFacade;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Delegate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TeamAuthFacadeAdapter implements TeamAuthFacade {
    private final @Delegate AuthenticationFacade delegate;
    private final UserService userService;

    @Override
    public Mono<Void> grantAuthorization(Collection<String> permissions) {
        try {
            Set<Permission> perms = permissions.stream().map(Permission::from).collect(Collectors.toUnmodifiableSet());
            return this.getConnectedUser().flatMap(user -> userService.grants(user.id, perms)).then();
        } catch (Exception e) {
            return Mono.error(() -> new IllegalArgumentException("Unable to grant permissions : " + permissions, e));
        }
    }

    @Override
    public Mono<Void> revokeAuthorization(Collection<String> permissions) {
        try {
            Set<Permission> perms = permissions.stream().map(Permission::from).collect(Collectors.toUnmodifiableSet());
            return this.getConnectedUser().flatMap(user -> userService.revokes(user.id, perms)).then();
        } catch (Exception e) {
            return Mono.error(() -> new IllegalArgumentException("Unable to revoke authorization " + permissions, e));
        }
    }
}
