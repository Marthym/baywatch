package fr.ght1pc9kc.baywatch.teams.infra.adapters;

import fr.ght1pc9kc.baywatch.security.api.AuthenticationFacade;
import fr.ght1pc9kc.baywatch.teams.domain.ports.TeamAuthFacade;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Delegate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TeamAuthFacadeAdapter implements TeamAuthFacade {
    private final @Delegate AuthenticationFacade delegate;
}
