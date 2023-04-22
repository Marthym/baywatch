package fr.ght1pc9kc.baywatch.teams.domain;

import com.github.f4b6a3.ulid.UlidFactory;
import fr.ght1pc9kc.baywatch.common.api.exceptions.UnauthorizedException;
import fr.ght1pc9kc.baywatch.common.api.model.Entity;
import fr.ght1pc9kc.baywatch.common.domain.EntityAssert;
import fr.ght1pc9kc.baywatch.security.api.model.Permission;
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
import java.util.stream.Collectors;

import static fr.ght1pc9kc.baywatch.common.api.model.EntitiesProperties.ID;
import static java.util.function.Predicate.not;

@RequiredArgsConstructor
public class TeamServiceImpl implements TeamsService {
    private static final String ID_PREFIX = "TM";
    private static final UlidFactory idGenerator = UlidFactory.newMonotonicInstance();

    private final TeamPersistencePort teamPersistence;
    private final TeamMemberPersistencePort teamMemberPersistence;
    private final TeamAuthFacade authFacade;

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
            String id = String.format("%s%s", ID_PREFIX, idGenerator.create());
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
                    .then(authFacade.grantAuthorization(manager.id, List.of(Permission.manager(id).toString())))
                    .thenReturn(id);
        }).flatMap(this::get);
    }

    @Override
    public Mono<Entity<Team>> update(String id, String name, String topic) {
        return authFacade.getConnectedUser()
                .filter(user -> RoleUtils.hasPermission(user.self, Permission.manager(id)))
                .switchIfEmpty(Mono.error(() -> new TeamPermissionDenied(
                        "You must be manager of the team to update it ! Try refresh to update permissions.")))
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
    public Flux<Entity<TeamMember>> members(PageRequest pgRequest) {
        return authFacade.getConnectedUser().flatMapMany(manager ->
                teamMemberPersistence.list(QueryContext.from(pgRequest)));
    }

    @Override
    public Flux<Entity<TeamMember>> addMembers(String id, Collection<String> membersIds) {
        Instant now = clock().instant();
        return authFacade.getConnectedUser()
                .flatMapMany(user -> {
                    boolean hasPermission = RoleUtils.hasPermission(user.self, Permission.manager(id));
                    boolean addHimself = membersIds.size() == 1 && membersIds.iterator().next().equals(user.id);
                    if (!hasPermission && !addHimself) {
                        return Flux.error(() -> new UnauthorizedException("You haven't any permission for this operation !"));
                    }
                    PendingFor pending = hasPermission && !addHimself ? PendingFor.USER : PendingFor.MANAGER;
                    return Flux.fromStream(membersIds.stream()
                            .map(mId -> Entity.<TeamMember>builder().id(id)
                                    .self(new TeamMember(mId, pending))
                                    .createdBy(user.id)
                                    .createdAt(now)
                                    .build()));
                }).collectList()
                .flatMap(teamMemberPersistence::add)
                .thenMany(teamMemberPersistence.list(QueryContext.all(Criteria.property(ID).eq(id))));
    }

    @Override
    public Flux<Entity<TeamMember>> removeMembers(String teamId, Collection<String> membersIds) {
        return authFacade.getConnectedUser()
                .filter(user -> RoleUtils.hasPermission(user.self, Permission.manager(teamId)) || (membersIds.stream().allMatch(user.id::equals)))
                .switchIfEmpty(Mono.error(() -> new TeamPermissionDenied("You must be manager of the team to remove users !")))
                .flatMapMany(manager -> ensureTeamKeepManager(manager.id, teamId, membersIds))
                // If no more team members, just remove the team and leave
                .flatMap(ignore -> teamMemberPersistence.list(QueryContext.all(Criteria.property(ID).eq(teamId))))
                .filter(not(e -> membersIds.contains(e.id)))
                .switchIfEmpty(delete(List.of(teamId)).thenMany(Flux.empty()))
                // else remove required members
                .then(authFacade.revokeAuthorization(Permission.manager(teamId).toString(), membersIds))
                .then(teamMemberPersistence.remove(teamId, membersIds))
                .thenMany(teamMemberPersistence.list(QueryContext.all(Criteria.property(ID).eq(teamId))));
    }

    /**
     * Throw an {@link IllegalArgumentException} if manager is the last manager of the given team
     *
     * @param managerId  The authorised user
     * @param teamId     The given team to check management
     * @param membersIds The members Ids to remove from management
     * @return The list of managerIds after filtering removed member
     * @throws IllegalArgumentException If the team has no more manager after filtering
     */
    private Flux<String> ensureTeamKeepManager(String managerId, String teamId, Collection<String> membersIds) {
        return authFacade.listManagers(teamId)
                .contextWrite(TeamAuthFacade.withSystemAuthentication(managerId))
                .filter(not(membersIds::contains))
                .switchIfEmpty(Flux.error(() -> new IllegalArgumentException(
                        "You are the last manager of the team, grant an other manager or remove the team")));
    }

    @Override
    public Mono<Void> promoteMember(String id, String memberId, boolean isManager) {
        if (!EntityAssert.user(memberId)) {
            return Mono.error(() -> new IllegalArgumentException(memberId + "is not a user ID !"));
        }
        if (!EntityAssert.team(id)) {
            return Mono.error(() -> new IllegalArgumentException(id + "is not a team ID !"));
        }
        return authFacade.getConnectedUser()
                .filter(user -> RoleUtils.hasPermission(user.self, Permission.manager(id)))
                .switchIfEmpty(Mono.error(() -> new TeamPermissionDenied("You must be manager of the team promote user !")))
                .flatMap(manager -> {
                    if (isManager) {
                        return authFacade.grantAuthorization(memberId, List.of(Permission.manager(id).toString()))
                                .contextWrite(TeamAuthFacade.withSystemAuthentication(manager.id));
                    } else {
                        return Mono.just(manager.id.equals(memberId))
                                .flatMapMany(isRevokeMe -> Boolean.TRUE.equals(isRevokeMe) ?
                                        ensureTeamKeepManager(manager.id, id, List.of(memberId)) :
                                        Flux.just(manager.id)).collectList()
                                .flatMap(ignore -> authFacade.revokeAuthorization(Permission.manager(id).toString(), List.of(memberId))
                                        .contextWrite(TeamAuthFacade.withSystemAuthentication(manager.id)));
                    }
                });
    }

    @Override
    public Flux<String> delete(Collection<String> ids) {
        return authFacade.getConnectedUser()
                .flatMapMany(user -> Flux.fromIterable(ids).filter(id -> RoleUtils.hasPermission(user.self, Permission.manager(id)))
                        .switchIfEmpty(Mono.error(() -> new TeamPermissionDenied("You must be manager of the team to delete it !")))
                        .collectList()
                        .flatMap(teamsIds ->
                                teamMemberPersistence.clear(teamsIds)
                                        .then(teamPersistence.delete(ids))
                                        .then(authFacade.removeAuthorizations(
                                                        teamsIds.stream()
                                                                .map(id -> Permission.manager(id).toString())
                                                                .collect(Collectors.toUnmodifiableSet())
                                                ).contextWrite(TeamAuthFacade.withSystemAuthentication(user.id))
                                        ).thenReturn(teamsIds))
                        .flatMapMany(Flux::fromIterable));
    }
}
