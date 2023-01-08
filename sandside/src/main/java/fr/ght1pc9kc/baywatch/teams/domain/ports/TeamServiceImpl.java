package fr.ght1pc9kc.baywatch.teams.domain.ports;

import com.github.f4b6a3.ulid.UlidFactory;
import fr.ght1pc9kc.baywatch.common.api.model.Entity;
import fr.ght1pc9kc.baywatch.teams.api.TeamsService;
import fr.ght1pc9kc.baywatch.teams.api.model.Team;
import fr.ght1pc9kc.baywatch.techwatch.domain.model.QueryContext;
import fr.ght1pc9kc.juery.api.Criteria;
import fr.ght1pc9kc.juery.api.PageRequest;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import static fr.ght1pc9kc.baywatch.common.api.model.EntitiesProperties.ID;

@RequiredArgsConstructor
public class TeamServiceImpl implements TeamsService {

    public static final String PREFIX = "TM";
    private final TeamPersistencePort teamPersistence;
    private final TeamMemberPersistencePort teamMemberPersistence;
    private final TeamAuthFacade authFacade;

    private final UlidFactory idGenerator = UlidFactory.newMonotonicInstance();

    @Override
    public Mono<Entity<Team>> get(String id) {
        return list(PageRequest.one(Criteria.property(ID).eq(id)))
                .singleOrEmpty();
    }

    @Override
    public Mono<Entity<Team>> create(String name, String topic) {
        return authFacade.getConnectedUser().flatMap(manager -> {
            String id = PREFIX + idGenerator.create().toString();
            return teamPersistence.persist(id, Team.of(name, topic))
                    .thenReturn(id);
        }).flatMap(this::get);
    }

    @Override
    public Flux<Entity<Team>> list(PageRequest pageRequest) {
        QueryContext qCtx = QueryContext.from(pageRequest);
        return teamPersistence.list(qCtx)
                .buffer(20)
                .flatMap(rawTeams -> {
                    Map<String, Entity<Team>> teams = rawTeams.stream().collect(Collectors.toUnmodifiableMap(e -> e.id, Function.identity()));
                    return listMembersFrom(QueryContext.all(qCtx.getFilter().and(Criteria.property(ID).in(teams.keySet()))))
                            .filter(t -> teams.containsKey(t.getT1()))
                            .map(t -> {
                                Entity<Team> entity = teams.get(t.getT1());
                                return Entity.<Team>builder()
                                        .self(Team.of(entity.self, t.getT2()))
                                        .createdBy(entity.createdBy)
                                        .createdAt(entity.createdAt)
                                        .build();
                            });
                });
    }

    private Flux<Tuple2<String, Set<String>>> listMembersFrom(QueryContext qCtx) {
        return authFacade.getConnectedUser().flatMapMany(manager ->
                        teamMemberPersistence.list(qCtx.withUserId(manager.id)))
                .bufferUntilChanged(Map.Entry::getKey)
                .map(this::unFlattenTeamMembers);
    }

    private Tuple2<String, Set<String>> unFlattenTeamMembers(List<Map.Entry<String, String>> teams) {
        return Tuples.of(teams.get(0).getKey(), teams.stream().map(Map.Entry::getValue).collect(Collectors.toUnmodifiableSet()));
    }

    @Override
    public Mono<Integer> count(PageRequest pageRequest) {
        return authFacade.getConnectedUser().flatMap(manager ->
                teamMemberPersistence.list(
                                QueryContext.all(pageRequest.filter()).withUserId(manager.id))
                        .map(Map.Entry::getKey)
                        .flatMap(ids -> teamPersistence.count(
                                QueryContext.all(pageRequest.filter().and(Criteria.property(ID).in(ids)))
                        )).reduce(Integer::sum));
    }

    @Override
    public Mono<Entity<Team>> update(String id, String name, String topic) {
        return null;
    }

    @Override
    public Flux<String> addMembers(String id, Collection<String> membersIds) {
        return null;
    }

    @Override
    public Flux<String> removeMembers(String id, Collection<String> membersIds) {
        return null;
    }

    @Override
    public Flux<Entity<Team>> delete(Collection<String> ids) {
        return null;
    }
}
