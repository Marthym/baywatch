package fr.ght1pc9kc.baywatch.teams.domain;

import fr.ght1pc9kc.baywatch.teams.api.TeamsService;
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
import org.mapstruct.factory.Mappers;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;
import reactor.test.StepVerifier;

import static org.mockito.Mockito.mock;
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
        when(mockAuthFacade.getConnectedUser()).thenReturn(Mono.just(UserSamples.YODA));

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
}