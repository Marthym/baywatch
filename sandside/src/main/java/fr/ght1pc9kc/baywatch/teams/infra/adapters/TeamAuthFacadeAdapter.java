package fr.ght1pc9kc.baywatch.teams.infra.adapters;

import fr.ght1pc9kc.baywatch.security.api.AuthenticationFacade;
import fr.ght1pc9kc.baywatch.security.api.UserService;
import fr.ght1pc9kc.baywatch.security.api.model.Role;
import fr.ght1pc9kc.baywatch.teams.domain.ports.TeamAuthFacade;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Delegate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class TeamAuthFacadeAdapter implements TeamAuthFacade {
    private final @Delegate AuthenticationFacade delegate;
    private final UserService userService;

    @Override
    public Mono<Void> grantAuthorization(String authorization) {
        try {
            String[] splitAuth = authorization.split(String.valueOf(Role.ENTITY_SEPARATOR));
            if (splitAuth.length != 2) {
                return Mono.error(() -> new IllegalArgumentException("Authorization must contain entity ID !"));
            }
            Role role = Role.valueOf(splitAuth[0]);
            return this.getConnectedUser().flatMap(user -> userService.grantRole(user.id, role, splitAuth[1])).then();
        } catch (Exception e) {
            return Mono.error(() -> new IllegalArgumentException("Unable to grant authorization " + authorization, e));
        }
    }
}
