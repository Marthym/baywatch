package fr.ght1pc9kc.baywatch.teams.domain;

import fr.ght1pc9kc.baywatch.common.api.model.Entity;
import fr.ght1pc9kc.baywatch.dsl.tables.Teams;
import fr.ght1pc9kc.baywatch.dsl.tables.TeamsMembers;
import fr.ght1pc9kc.baywatch.dsl.tables.records.TeamsRecord;
import fr.ght1pc9kc.baywatch.security.api.model.Role;
import fr.ght1pc9kc.baywatch.security.api.model.User;
import fr.ght1pc9kc.baywatch.teams.api.TeamsService;
import fr.ght1pc9kc.baywatch.teams.api.exceptions.TeamPermissionDenied;
import fr.ght1pc9kc.baywatch.teams.domain.model.PendingFor;
import fr.ght1pc9kc.baywatch.teams.domain.ports.TeamAuthFacade;
import fr.ght1pc9kc.baywatch.teams.infra.adapters.MembersPersistenceAdapter;
import fr.ght1pc9kc.baywatch.teams.infra.adapters.TeamPersistenceAdapter;
import fr.ght1pc9kc.baywatch.teams.infra.mappers.TeamsMapper;
import fr.ght1pc9kc.baywatch.teams.infra.samples.TeamsMembersRecordSamples;
import fr.ght1pc9kc.baywatch.teams.infra.samples.TeamsRecordSamples;
import fr.ght1pc9kc.baywatch.tests.samples.UserSamples;
import fr.ght1pc9kc.baywatch.tests.samples.infra.UsersRecordSamples;
import fr.ght1pc9kc.baywatch.tests.samples.infra.UsersRolesSamples;
import fr.ght1pc9kc.juery.api.PageRequest;
import fr.ght1pc9kc.testy.core.extensions.ChainedExtension;
import fr.ght1pc9kc.testy.jooq.WithDatabaseLoaded;
import fr.ght1pc9kc.testy.jooq.WithDslContext;
import fr.ght1pc9kc.testy.jooq.WithInMemoryDatasource;
import fr.ght1pc9kc.testy.jooq.WithSampleDataLoaded;
import fr.ght1pc9kc.testy.jooq.WithSampleDataLoaded.Tracker;
import org.assertj.core.api.Assertions;
import org.jooq.DSLContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mapstruct.factory.Mappers;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;
import reactor.test.StepVerifier;

import java.util.List;
import java.util.stream.Stream;

import static fr.ght1pc9kc.baywatch.teams.infra.samples.TeamsRecordSamples.JEDI_TEAM;
import static java.util.Objects.requireNonNull;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Named.named;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class TeamServiceImplTest {
    private static final WithInMemoryDatasource wDs = WithInMemoryDatasource.builder().build();
    private static final WithDatabaseLoaded wBaywatchDb = WithDatabaseLoaded.builder()
            .setDatasourceExtension(wDs)
            .useFlywayDefaultLocation()
            .build();
    private static final WithDslContext wDslContext = WithDslContext.builder()
            .setDatasourceExtension(wDs).build();
    private static final WithSampleDataLoaded wSamples = WithSampleDataLoaded.builder(wDslContext)
            .addDataset(UsersRecordSamples.SAMPLE)
            .addDataset(UsersRolesSamples.SAMPLE)
            .addDataset(TeamsRecordSamples.SAMPLE)
            .addDataset(TeamsMembersRecordSamples.SAMPLE)
            .build();

    @RegisterExtension
    @SuppressWarnings("unused")
    static ChainedExtension chain = ChainedExtension.outer(wDs)
            .append(wBaywatchDb)
            .append(wDslContext)
            .append(wSamples)
            .register();

    private final TeamAuthFacade mockAuthFacade = mock(TeamAuthFacade.class);

    private TeamsService tested;

    @BeforeEach
    void setUp(DSLContext dsl) {
        when(mockAuthFacade.getConnectedUser()).thenReturn(Mono.just(UserSamples.LUKE));
        when(mockAuthFacade.grantAuthorization(any())).thenReturn(Mono.empty().then());
        when(mockAuthFacade.revokeAuthorization(any())).thenReturn(Mono.empty().then());

        Scheduler immediate = Schedulers.immediate();
        TeamsMapper mapper = Mappers.getMapper(TeamsMapper.class);
        tested = new TeamServiceImpl(
                new TeamPersistenceAdapter(immediate, dsl, mapper),
                new MembersPersistenceAdapter(immediate, dsl, mapper),
                mockAuthFacade);
    }

    @Test
    void should_get_team(Tracker dbTracker) {
        dbTracker.skipNextSampleLoad();

        StepVerifier.create(tested.get("TM01GP696RFPTY32WD79CVB0KDTF"))
                .assertNext(next -> Assertions.assertThat(next.id).isEqualTo("TM01GP696RFPTY32WD79CVB0KDTF"))
                .verifyComplete();
    }

    @Test
    void should_list_teams(Tracker dbTracker) {
        dbTracker.skipNextSampleLoad();
        when(mockAuthFacade.getConnectedUser()).thenReturn(Mono.just(UserSamples.LUKE));

        StepVerifier.create(tested.list(PageRequest.all()))
                .assertNext(next -> Assertions.assertThat(next.id).isEqualTo("TM01GP696RFPTY32WD79CVB0KDTF"))
                .verifyComplete();
    }

    @Test
    void should_count_teams(Tracker dbTracker) {
        dbTracker.skipNextSampleLoad();
        when(mockAuthFacade.getConnectedUser()).thenReturn(Mono.just(UserSamples.LUKE));

        StepVerifier.create(tested.count(PageRequest.all()))
                .assertNext(next -> Assertions.assertThat(next).isEqualTo(1))
                .verifyComplete();
    }

    @Test
    void should_create_team() {
        when(mockAuthFacade.getConnectedUser()).thenReturn(Mono.just(UserSamples.OBIWAN));
        StepVerifier.create(tested.create("Jedi council team", "May the Force be with you"))
                .assertNext(actual -> assertAll(
                        () -> Assertions.assertThat(actual.id).isNotBlank(),
                        () -> Assertions.assertThat(actual.createdBy).isEqualTo(UserSamples.OBIWAN.id),
                        () -> Assertions.assertThat(actual.self.name()).isEqualTo("Jedi council team"),
                        () -> verify(mockAuthFacade).grantAuthorization(List.of("MANAGER:" + actual.id))
                )).verifyComplete();
    }

    @ParameterizedTest
    @MethodSource({"allowedTeamModificationUsers"})
    void should_update_managed_team(Entity<User> manager, DSLContext dsl) {
        when(mockAuthFacade.getConnectedUser()).thenReturn(Mono.just(manager));

        StepVerifier.create(tested.update(JEDI_TEAM.getTeamId(), "Luke Skywalker Team", "The new topic for Skywalker"))
                .assertNext(actual -> assertAll(
                        () -> Assertions.assertThat(actual.id).isEqualTo(JEDI_TEAM.getTeamId()),
                        () -> Assertions.assertThat(actual.self.name()).isEqualTo("Luke Skywalker Team"),
                        () -> Assertions.assertThat(actual.self.topic()).isEqualTo("The new topic for Skywalker")
                )).verifyComplete();

        TeamsRecord actual = dsl.selectFrom(Teams.TEAMS).where(Teams.TEAMS.TEAM_ID.eq(JEDI_TEAM.getTeamId()))
                .fetchOne();
        assertAll(
                () -> Assertions.assertThat(actual).isNotNull(),
                () -> Assertions.assertThat(actual).isNotNull().extracting(TeamsRecord::getTeamId).isEqualTo(JEDI_TEAM.getTeamId()),
                () -> Assertions.assertThat(actual).isNotNull().extracting(TeamsRecord::getTeamName).isEqualTo("Luke Skywalker Team"),
                () -> Assertions.assertThat(actual).isNotNull().extracting(TeamsRecord::getTeamTopic).isEqualTo("The new topic for Skywalker")
        );
    }

    static Stream<Arguments> allowedTeamModificationUsers() {
        return Stream.of(
                arguments(named(UserSamples.LUKE.self.name, UserSamples.LUKE)),
                arguments(named(UserSamples.YODA.self.name, UserSamples.YODA))
        );
    }

    @Test
    void should_fail_to_update_unmanaged_team() {
        when(mockAuthFacade.getConnectedUser()).thenReturn(Mono.just(UserSamples.OBIWAN));

        StepVerifier.create(tested.update(JEDI_TEAM.getTeamId(), "Luke Skywalker Team", "The new topic for Skywalker"))
                .verifyError(TeamPermissionDenied.class);
    }

    @Test
    void should_list_team_members() {
        StepVerifier.create(tested.members(JEDI_TEAM.getTeamId()).map(e -> e.self.userId()).collectList())
                .assertNext(actual -> Assertions.assertThat(actual).containsOnly(
                        UserSamples.LUKE.id,
                        UserSamples.OBIWAN.id
                )).verifyComplete();
    }

    @ParameterizedTest
    @MethodSource("addMembersProfiles")
    void should_add_member(Entity<User> user, PendingFor expectedPendingFor) {
        when(mockAuthFacade.getConnectedUser()).thenReturn(Mono.just(user));

        StepVerifier.create(tested.addMembers(JEDI_TEAM.getTeamId(), List.of(UserSamples.DSIDIOUS.id)).collectList())
                .assertNext(actual -> assertAll(
                        () -> Assertions.assertThat(actual).isNotNull().isNotEmpty(),
                        () -> Assertions.assertThat(actual).extracting(e -> e.self.userId()).containsOnly(
                                UserSamples.DSIDIOUS.id, UserSamples.LUKE.id, UserSamples.OBIWAN.id),
                        () -> Assertions.assertThat(requireNonNull(actual).stream()
                                        .filter(e -> e.self.userId().equals(UserSamples.DSIDIOUS.id))
                                        .findFirst()).isPresent()
                                .map(e -> e.self.pending()).contains(expectedPendingFor)
                )).verifyComplete();
    }

    static Stream<Arguments> addMembersProfiles() {
        return Stream.of(
                arguments(named(UserSamples.DSIDIOUS.self.name, UserSamples.DSIDIOUS), PendingFor.MANAGER),
                arguments(named(UserSamples.LUKE.self.name, UserSamples.LUKE), PendingFor.USER)
        );
    }

    @ParameterizedTest
    @MethodSource("removeMembersProfiles")
    void should_remove_member_as(Entity<User> user) {
        when(mockAuthFacade.getConnectedUser()).thenReturn(Mono.just(user));

        StepVerifier.create(tested.removeMembers(JEDI_TEAM.getTeamId(), List.of(UserSamples.OBIWAN.id)).collectList())
                .assertNext(actual -> assertAll(
                        () -> Assertions.assertThat(actual).isNotNull().isNotEmpty(),
                        () -> Assertions.assertThat(actual).extracting(e -> e.self.userId())
                                .containsOnly(UserSamples.LUKE.id)
                )).verifyComplete();
    }

    static Stream<Arguments> removeMembersProfiles() {
        return Stream.of(
                arguments(named(Role.USER.name(), UserSamples.OBIWAN)),
                arguments(named(Role.MANAGER.name(), UserSamples.LUKE)),
                arguments(named(Role.ADMIN.name(), UserSamples.YODA))
        );
    }

    @Test
    void should_fail_to_remove_member_as_unauthorized_user() {
        when(mockAuthFacade.getConnectedUser()).thenReturn(Mono.just(UserSamples.DSIDIOUS));

        StepVerifier.create(tested.removeMembers(JEDI_TEAM.getTeamId(), List.of(UserSamples.OBIWAN.id)).collectList())
                .verifyError(TeamPermissionDenied.class);
    }

    @Test
    void should_delete_team(DSLContext dsl) {
        StepVerifier.create(tested.delete(List.of(JEDI_TEAM.getTeamId())))
                .assertNext(actual -> Assertions.assertThat(actual).isEqualTo(JEDI_TEAM.getTeamId()))
                .verifyComplete();

        Assertions.assertThat(dsl.fetchCount(Teams.TEAMS, Teams.TEAMS.TEAM_ID.eq(JEDI_TEAM.getTeamId())))
                .as("Ensure the team was deleted from database")
                .isZero();
        Assertions.assertThat(dsl.fetchCount(TeamsMembers.TEAMS_MEMBERS, TeamsMembers.TEAMS_MEMBERS.TEME_TEAM_ID.eq(JEDI_TEAM.getTeamId())))
                .as("Ensure all team members was deleted from database")
                .isZero();
    }
}