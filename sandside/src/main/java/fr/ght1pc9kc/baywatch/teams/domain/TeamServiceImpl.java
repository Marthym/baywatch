package fr.ght1pc9kc.baywatch.teams.domain;

import com.github.f4b6a3.ulid.UlidFactory;
import fr.ght1pc9kc.baywatch.common.api.model.Entity;
import fr.ght1pc9kc.baywatch.security.api.model.Role;
import fr.ght1pc9kc.baywatch.security.api.model.RoleUtils;
import fr.ght1pc9kc.baywatch.teams.api.TeamsService;
import fr.ght1pc9kc.baywatch.teams.api.exceptions.TeamPermissionDenied;
import fr.ght1pc9kc.baywatch.teams.api.model.Team;
import fr.ght1pc9kc.baywatch.teams.api.model.TeamMember;
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

import java.time.Clock;
import java.time.Instant;
import java.util.Collection;
import java.util.List;

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
    public Flux<Entity<Team>> list(PageRequest pageRequest) {
        return authFacade.getConnectedUser().flatMapMany(member -> {
            QueryContext qCtx = QueryContext.builder()
                    .filter(pageRequest.filter())
                    .userId(member.id)
                    .build();
            return teamMemberPersistence.list(qCtx)
                    .map(e -> e.id)
                    .concatWith(Flux.fromIterable(RoleUtils.getEntitiesFor(member.self, Role.MANAGER)));

        }).collectList().flatMapMany(ids -> {
            QueryContext qCtx2 = QueryContext.builder()
                    .filter(pageRequest.filter().and(Criteria.property(ID).in(ids)))
                    .pagination(pageRequest.pagination())
                    .build();
            return teamPersistence.list(qCtx2);
        });
    }

    @Override
    public Mono<Integer> count(PageRequest pageRequest) {
        return authFacade.getConnectedUser().flatMapMany(member -> {
            QueryContext qCtx = QueryContext.builder()
                    .filter(pageRequest.filter())
                    .userId(member.id)
                    .build();
            return teamMemberPersistence.list(qCtx)
                    .map(e -> e.id)
                    .concatWith(Flux.fromIterable(RoleUtils.getEntitiesFor(member.self, Role.MANAGER)));

        }).collectList().flatMap(ids -> {
            QueryContext qCtx2 = QueryContext.builder()
                    .filter(pageRequest.filter().and(Criteria.property(ID).in(ids)))
                    .pagination(pageRequest.pagination())
                    .build();
            return teamPersistence.count(qCtx2);
        });
    }

    @Override
    public Mono<Entity<Team>> create(String name, String topic) {
        Instant now = clock().instant();
        return authFacade.getConnectedUser().flatMap(manager -> {
            String id = PREFIX + idGenerator.create().toString();
            return teamPersistence.persist(Entity.<Team>builder()
                            .id(id)
                            .self(new Team(name, topic))
                            .createdAt(now)
                            .createdBy(manager.id)
                            .build())
                    .then(teamMemberPersistence.add(List.of(Entity.<TeamMember>builder().id(id)
                            .self(new TeamMember(manager.id, PendingFor.NONE))
                            .createdBy(manager.id)
                            .createdAt(now)
                            .build())))
                    .then(authFacade.grantAuthorization(Role.manager(id)))
                    .thenReturn(id);
        }).flatMap(this::get);
    }

    @Override
    public Mono<Entity<Team>> update(String id, String name, String topic) {
        return authFacade.getConnectedUser()
                .filter(user -> RoleUtils.hasRole(user.self, Role.MANAGER, id))
                .switchIfEmpty(Mono.error(() -> new TeamPermissionDenied("You must be manager of the team to update it !")))
                .flatMap(manager ->
                        teamPersistence.persist(Entity.<Team>builder()
                                .id(id)
                                .self(new Team(name, topic))
                                .createdAt(clock().instant())
                                .createdBy(manager.id)
                                .build()))
                .then(teamPersistence.list(QueryContext.id(id)).next());
    }

    @Override
    public Flux<Entity<TeamMember>> members(String id) {
        return authFacade.getConnectedUser().flatMapMany(manager ->
                teamMemberPersistence.list(QueryContext.id(id)));
    }

    @Override
    public Flux<Entity<TeamMember>> addMembers(String id, Collection<String> membersIds) {
        Instant now = clock().instant();
        return authFacade.getConnectedUser()
                .flatMapMany(user -> {
                    PendingFor pending = RoleUtils.hasRole(user.self, Role.MANAGER, id) ? PendingFor.USER : PendingFor.MANAGER;
                    return Flux.fromStream(membersIds.stream()
                            .map(mId -> Entity.<TeamMember>builder().id(id)
                                    .self(new TeamMember(mId, pending))
                                    .createdBy(user.id)
                                    .createdAt(now)
                                    .build()));
                }).collectList()
                .flatMap(teamMemberPersistence::add)
                .thenMany(teamMemberPersistence.list(QueryContext.id(id)));
    }

    @Override
    public Flux<Entity<TeamMember>> removeMembers(String id, Collection<String> membersIds) {
        return authFacade.getConnectedUser()
                .filter(user -> RoleUtils.hasRole(user.self, Role.MANAGER, id))
                .switchIfEmpty(Mono.error(() -> new TeamPermissionDenied("You must be manager of the team to remove users !")))
                .flatMap(ignore -> teamMemberPersistence.remove(id, membersIds))
                .thenMany(teamMemberPersistence.list(QueryContext.id(id)));
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
