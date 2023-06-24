package fr.ght1pc9kc.baywatch.techwatch.infra.persistence;

import fr.ght1pc9kc.baywatch.common.api.model.Entity;
import fr.ght1pc9kc.baywatch.common.domain.Hasher;
import fr.ght1pc9kc.baywatch.techwatch.api.model.Flags;
import fr.ght1pc9kc.baywatch.techwatch.api.model.State;
import fr.ght1pc9kc.baywatch.techwatch.domain.model.QueryContext;
import fr.ght1pc9kc.baywatch.tests.samples.infra.FeedRecordSamples;
import fr.ght1pc9kc.baywatch.tests.samples.infra.FeedsUsersRecordSample;
import fr.ght1pc9kc.baywatch.tests.samples.infra.NewsRecordSamples;
import fr.ght1pc9kc.baywatch.tests.samples.infra.UsersRecordSamples;
import fr.ght1pc9kc.baywatch.tests.samples.infra.UsersRolesSamples;
import fr.ght1pc9kc.juery.api.Criteria;
import fr.ght1pc9kc.testy.core.extensions.ChainedExtension;
import fr.ght1pc9kc.testy.jooq.WithDslContext;
import fr.ght1pc9kc.testy.jooq.WithInMemoryDatasource;
import fr.ght1pc9kc.testy.jooq.WithSampleDataLoaded;
import org.jooq.DSLContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import reactor.core.scheduler.Schedulers;

import java.util.List;

import static fr.ght1pc9kc.baywatch.dsl.tables.NewsUserState.NEWS_USER_STATE;
import static fr.ght1pc9kc.baywatch.tests.samples.infra.UsersRecordSamples.LSKYWALKER;
import static fr.ght1pc9kc.baywatch.tests.samples.infra.UsersRecordSamples.OKENOBI;
import static org.assertj.core.api.Assertions.assertThat;

class StateRepositoryTest {
    private static final WithInMemoryDatasource wDs = WithInMemoryDatasource.builder().build();

    private static final WithDslContext wDslContext = WithDslContext.builder()
            .setDatasourceExtension(wDs).build();
    private static final WithSampleDataLoaded wSamples = WithSampleDataLoaded.builder(wDslContext)
            .createTablesIfNotExists()
            .addDataset(UsersRecordSamples.SAMPLE)
            .addDataset(UsersRolesSamples.SAMPLE)
            .addDataset(FeedRecordSamples.SAMPLE)
            .addDataset(NewsRecordSamples.SAMPLE)
            .addDataset(NewsRecordSamples.NewsFeedsRecordSample.SAMPLE)
            .addDataset(NewsRecordSamples.NewsUserStateSample.SAMPLE)
            .addDataset(FeedsUsersRecordSample.SAMPLE)
            .build();

    @RegisterExtension
    @SuppressWarnings("unused")
    static ChainedExtension chain = ChainedExtension.outer(wDs)
            .append(wDslContext)
            .append(wSamples)
            .register();

    private StateRepository tested;

    @BeforeEach
    void setUp(DSLContext dslContext) {
        tested = new StateRepository(Schedulers.immediate(), dslContext);
    }

    @Test
    void should_list_state(WithSampleDataLoaded.Tracker tracker) {
        tracker.skipNextSampleLoad();

        String id21 = Hasher.identify(NewsRecordSamples.BASE_TEST_URI.resolve("021"));
        String id22 = Hasher.identify(NewsRecordSamples.BASE_TEST_URI.resolve("022"));
        String id23 = Hasher.identify(NewsRecordSamples.BASE_TEST_URI.resolve("023"));
        List<Entity<State>> actuals = tested.list(QueryContext.all(
                Criteria.property("newsId").in(id21, id22, id23))
        ).collectList().block();

        assertThat(actuals).containsOnly(
                Entity.identify(id21, OKENOBI.getUserId(), State.of(Flags.NONE)),
                Entity.identify(id22, LSKYWALKER.getUserId(), State.of(Flags.READ)),
                Entity.identify(id23, OKENOBI.getUserId(), State.of(Flags.SHARED))
        );
    }

    @ParameterizedTest
    @CsvSource({
            Flags.NONE + ", " + Flags.SHARED + ", false, true",
            Flags.ALL + ", " + Flags.SHARED + ", true, true",
            Flags.SHARED + ", " + Flags.SHARED + ", false, true",
            Flags.READ + ", " + Flags.SHARED + ", true, true",
            Flags.NONE + ", " + Flags.READ + ", true, false",
            Flags.ALL + ", " + Flags.READ + ", true, true",
            Flags.SHARED + ", " + Flags.READ + ", true, true",
            Flags.READ + ", " + Flags.READ + ", true, false",
    })
    void should_add_state_flag(int startState, int removeFlag, boolean expectedRead, boolean expectedShared, DSLContext dsl) {
        final String newsId = "37c8fbce87cae77f34aac2a2a52511f60b1369317dec57f38df3f3ae30c42840";
        dsl.update(NEWS_USER_STATE)
                .set(NEWS_USER_STATE.NURS_STATE, startState)
                .where(NEWS_USER_STATE.NURS_NEWS_ID.eq(newsId))
                .execute();

        tested.flag(newsId,
                OKENOBI.getUserId(), removeFlag).block();

        Integer actual = dsl.select(NEWS_USER_STATE.NURS_STATE)
                .from(NEWS_USER_STATE)
                .where(NEWS_USER_STATE.NURS_NEWS_ID.eq(newsId))
                .fetchOne(NEWS_USER_STATE.NURS_STATE);

        assertThat(State.of(actual).isRead()).isEqualTo(expectedRead);
        assertThat(State.of(actual).isShared()).isEqualTo(expectedShared);
    }

    @ParameterizedTest
    @CsvSource({
            Flags.NONE + ", " + Flags.SHARED + ", false, false",
            Flags.ALL + ", " + Flags.SHARED + ", true, false",
            Flags.SHARED + ", " + Flags.SHARED + ", false, false",
            Flags.READ + ", " + Flags.SHARED + ", true, false",
            Flags.NONE + ", " + Flags.READ + ", false, false",
            Flags.ALL + ", " + Flags.READ + ", false, true",
            Flags.SHARED + ", " + Flags.READ + ", false, true",
            Flags.READ + ", " + Flags.READ + ", false, false",
    })
    void should_remove_state_flag(int startState, int removeFlag, boolean expectedRead, boolean expectedShared, DSLContext dsl) {
        final String newsId = "900cf7d10afd3c1584d6d3122743a86c0315fde7d8acbe3a585a2cb7c301807c";
        dsl.update(NEWS_USER_STATE)
                .set(NEWS_USER_STATE.NURS_STATE, startState)
                .where(NEWS_USER_STATE.NURS_NEWS_ID.eq(newsId))
                .execute();

        tested.unflag(newsId,
                UsersRecordSamples.LSKYWALKER.getUserId(), removeFlag).block();

        Integer actual = dsl.select(NEWS_USER_STATE.NURS_STATE)
                .from(NEWS_USER_STATE)
                .where(NEWS_USER_STATE.NURS_NEWS_ID.eq(newsId))
                .fetchOne(NEWS_USER_STATE.NURS_STATE);

        assertThat(State.of(actual).isRead()).isEqualTo(expectedRead);
        assertThat(State.of(actual).isShared()).isEqualTo(expectedShared);
    }
}