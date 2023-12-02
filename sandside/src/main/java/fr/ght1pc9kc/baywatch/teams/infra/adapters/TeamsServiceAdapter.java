package fr.ght1pc9kc.baywatch.teams.infra.adapters;

import fr.ght1pc9kc.baywatch.teams.api.TeamsService;
import fr.ght1pc9kc.baywatch.teams.domain.TeamServiceImpl;
import fr.ght1pc9kc.baywatch.teams.domain.ports.TeamAuthFacade;
import fr.ght1pc9kc.baywatch.teams.domain.ports.TeamMemberPersistencePort;
import fr.ght1pc9kc.baywatch.teams.domain.ports.TeamPersistencePort;
import lombok.experimental.Delegate;
import org.springframework.stereotype.Service;

@Service
public class TeamsServiceAdapter implements TeamsService {
    @Delegate
    private final TeamsService delegate;

    public TeamsServiceAdapter(TeamPersistencePort persistencePort, TeamMemberPersistencePort teamMemberPersistencePort,
                               TeamAuthFacade teamAuthFacade) {
        this.delegate = new TeamServiceImpl(persistencePort, teamMemberPersistencePort, teamAuthFacade);
    }
}
