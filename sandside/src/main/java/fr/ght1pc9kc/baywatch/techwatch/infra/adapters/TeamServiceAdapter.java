package fr.ght1pc9kc.baywatch.techwatch.infra.adapters;

import fr.ght1pc9kc.baywatch.common.api.model.Entity;
import fr.ght1pc9kc.baywatch.teams.api.TeamsService;
import fr.ght1pc9kc.baywatch.teams.domain.model.PendingFor;
import fr.ght1pc9kc.baywatch.techwatch.domain.ports.TeamServicePort;
import fr.ght1pc9kc.juery.api.Criteria;
import fr.ght1pc9kc.juery.api.PageRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

import static fr.ght1pc9kc.baywatch.common.api.model.EntitiesProperties.ID;

@Component
@RequiredArgsConstructor
public class TeamServiceAdapter implements TeamServicePort {

    private final TeamsService teamsService;

    @Override
    public Flux<String> getTeamMates(String userId) {
        return teamsService.list(PageRequest.all())
                .map(Entity::id)
                .collectList().flatMapMany(teamsId ->
                        teamsService.members(PageRequest.all(Criteria.property(ID).in(teamsId))))
                .filter(t -> t.self().pending().equals(PendingFor.NONE))
                .map(t -> t.self().userId());
    }
}
