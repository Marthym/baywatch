package fr.ght1pc9kc.baywatch.teams.domain;

import com.github.f4b6a3.ulid.UlidFactory;
import fr.ght1pc9kc.baywatch.common.api.model.Entity;
import fr.ght1pc9kc.baywatch.security.api.model.Role;
import fr.ght1pc9kc.baywatch.security.api.model.RoleUtils;
import fr.ght1pc9kc.baywatch.teams.api.TeamsService;
import fr.ght1pc9kc.baywatch.teams.api.exceptions.TeamPermissionDenied;
import fr.ght1pc9kc.baywatch.teams.api.model.Team;
import fr.ght1pc9kc.baywatch.teams.domain.model.PendingFor;
import fr.ght1pc9kc.baywatch.teams.domain.ports.TeamAuthFacade;
import fr.ght1pc9kc.baywatch.teams.domain.ports.TeamMemberPersistencePort;
import fr.ght1pc9kc.baywatch.teams.domain.ports.TeamPersistencePort;
import fr.ght1pc9kc.baywatch.techwatch.domain.model.QueryContext;
import fr.ght1pc9kc.juery.api.Criteria;
import fr.ght1pc9kc.juery.api.PageRequest;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import org.jetbrains.annotations.VisibleForTesting;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

import java.time.Clock;
import java.time.Instant;
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

    @Accessors(fluent = true)
    @Getter(value = AccessLevel.PRIVATE, onMethod = @__(@VisibleForTesting))
    private final Clock clock = Clock.systemUTC();

    @Override
    public Mono<Entity<Team>> get(String id) {
        return list(PageRequest.one(Criteria.property(ID).eq(id)))
                .singleOrEmpty();
    }

    @Override
    public Mono<Entity<Team>> create(String name, String topic) {
        return authFacade.getConnectedUser().flatMap(manager -> {
            String id = PREFIX + idGenerator.create().toString();
            return teamPersistence.persist(Entity.<Team>builder()
                            .id(id)
                            .self(Team.of(name, topic))
                            .createdAt(clock().instant())
                            .createdBy(manager.id)
                            .build())
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
        return authFacade.getConnectedUser()
                .filter(user -> RoleUtils.hasRole(user.self, Role.MANAGER, id))
                .switchIfEmpty(Mono.error(() -> new TeamPermissionDenied("You must be manager of the team to update it !")))
                .flatMap(manager ->
                        teamPersistence.persist(Entity.<Team>builder()
                                .id(id)
                                .self(Team.of(name, topic))
                                .createdAt(clock().instant())
                                .createdBy(manager.id)
                                .build()))
                .then(teamPersistence.list(QueryContext.id(id)).next());
    }

    @Override
    public Flux<String> addMembers(String id, Collection<String> membersIds) {
        Instant now = clock().instant();
        return authFacade.getConnectedUser()
                .flatMapMany(user -> {
                    PendingFor pending = RoleUtils.hasRole(user.self, Role.MANAGER, id) ? PendingFor.USER : PendingFor.MANAGER;
                    return Flux.fromStream(membersIds.stream()
                            .map(mId -> Entity.<PendingFor>builder().id(id + ":" + mId)
                                    .self(pending)
                                    .createdBy(user.id)
                                    .createdAt(now)
                                    .build()));
                }).collectList()
                .flatMap(teamMemberPersistence::add)
                .thenMany(teamMemberPersistence.list(QueryContext.id(id)).map(Map.Entry::getValue));
    }

    @Override
    public Flux<String> removeMembers(String id, Collection<String> membersIds) {
        return authFacade.getConnectedUser()
                .filter(user -> RoleUtils.hasRole(user.self, Role.MANAGER, id))
                .switchIfEmpty(Mono.error(() -> new TeamPermissionDenied("You must be manager of the team to remove users !")))
                .flatMap(ignore -> teamMemberPersistence.remove(id, membersIds))
                .thenMany(teamMemberPersistence.list(QueryContext.id(id)).map(Map.Entry::getValue));
    }

    @Override
    public Flux<String> delete(Collection<String> ids) {
        return authFacade.getConnectedUser()
                .flatMapMany(user -> Flux.fromIterable(ids).filter(id -> RoleUtils.hasRole(user.self, Role.MANAGER, id)))
                .switchIfEmpty(Mono.error(() -> new TeamPermissionDenied("You must be manager of the team to delete it !")))
                .collectList()
                .flatMap(teamsIds ->
                        teamMemberPersistence.clear(teamsIds)
                                .then(teamPersistence.delete(ids))
                                .thenReturn(teamsIds))
                .flatMapMany(Flux::fromIterable);
    }
}
