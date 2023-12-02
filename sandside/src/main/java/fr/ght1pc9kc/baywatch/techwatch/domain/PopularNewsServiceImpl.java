package fr.ght1pc9kc.baywatch.techwatch.domain;

import fr.ght1pc9kc.baywatch.security.api.AuthenticationFacade;
import fr.ght1pc9kc.baywatch.techwatch.api.PopularNewsService;
import fr.ght1pc9kc.baywatch.techwatch.api.model.Popularity;
import fr.ght1pc9kc.baywatch.techwatch.domain.model.QueryContext;
import fr.ght1pc9kc.baywatch.techwatch.domain.ports.TeamServicePort;
import fr.ght1pc9kc.baywatch.techwatch.infra.persistence.StateRepository;
import fr.ght1pc9kc.juery.api.Criteria;
import fr.ght1pc9kc.juery.api.Pagination;
import fr.ght1pc9kc.juery.api.pagination.Direction;
import fr.ght1pc9kc.juery.api.pagination.Sort;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;

import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

import static fr.ght1pc9kc.baywatch.common.api.model.EntitiesProperties.NEWS_ID;
import static fr.ght1pc9kc.baywatch.common.api.model.EntitiesProperties.SHARED;
import static fr.ght1pc9kc.baywatch.common.api.model.EntitiesProperties.USER_ID;

@RequiredArgsConstructor
public class PopularNewsServiceImpl implements PopularNewsService {

    private final StateRepository stateRepository;
    private final AuthenticationFacade authFacade;
    private final TeamServicePort teamServicePort;

    @Override
    public Flux<Popularity> get(Collection<String> ids) {
        return authFacade.getConnectedUser()
                .flatMapMany(user -> teamServicePort.getTeamMates(user.id))
                .collectList().map(teamMates -> QueryContext.builder()
                        .filter(Criteria.property(NEWS_ID).in(ids)
                                .and(Criteria.property(USER_ID).in(teamMates))
                                .and(Criteria.property(SHARED).eq(true)))
                        .pagination(Pagination.of(-1, -1, Sort.of(Direction.ASC, NEWS_ID)))
                        .build())
                .flatMapMany(stateRepository::list)
                .bufferUntilChanged(s -> s.id)
                .map(states -> {
                    Set<String> fans = states.stream().map(s -> s.createdBy).collect(Collectors.toUnmodifiableSet());
                    return new Popularity(states.get(0).id, fans.size(), fans);
                });
    }
}
