package fr.ght1pc9kc.baywatch.infra.adapters.persistence;

import fr.ght1pc9kc.baywatch.api.model.User;
import fr.ght1pc9kc.baywatch.api.model.request.PageRequest;
import fr.ght1pc9kc.baywatch.api.model.request.filter.Criteria;
import fr.ght1pc9kc.baywatch.domain.utils.Hasher;
import fr.ght1pc9kc.baywatch.dsl.tables.Users;
import fr.ght1pc9kc.baywatch.infra.mappers.BaywatchMapper;
import fr.ght1pc9kc.baywatch.infra.samples.FeedRecordSamples;
import fr.ght1pc9kc.baywatch.infra.samples.FeedsUsersRecordSample;
import fr.ght1pc9kc.baywatch.infra.samples.NewsRecordSamples;
import fr.ght1pc9kc.baywatch.infra.samples.UsersRecordSamples;
import fr.irun.testy.core.extensions.ChainedExtension;
import fr.irun.testy.jooq.WithDatabaseLoaded;
import fr.irun.testy.jooq.WithDslContext;
import fr.irun.testy.jooq.WithInMemoryDatasource;
import fr.irun.testy.jooq.WithSampleDataLoaded;
import org.jooq.DSLContext;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.mapstruct.factory.Mappers;
import reactor.core.scheduler.Schedulers;

import java.util.List;

import static fr.ght1pc9kc.baywatch.dsl.tables.FeedsUsers.FEEDS_USERS;
import static fr.ght1pc9kc.baywatch.dsl.tables.NewsUserState.NEWS_USER_STATE;
import static org.assertj.core.api.Assertions.assertThat;

class UserRepositoryTest {
    private static final WithInMemoryDatasource wDs = WithInMemoryDatasource.builder().build();
    private static final WithDatabaseLoaded wBaywatchDb = WithDatabaseLoaded.builder()
            .setDatasourceExtension(wDs)
            .useFlywayDefaultLocation()
            .build();
    private static final WithDslContext wDslContext = WithDslContext.builder()
            .setDatasourceExtension(wDs).build();
    private static final WithSampleDataLoaded wSamples = WithSampleDataLoaded.builder(wDslContext)
            .addDataset(UsersRecordSamples.SAMPLE)
            .addDataset(FeedRecordSamples.SAMPLE)
            .addDataset(NewsRecordSamples.SAMPLE)
            .addDataset(FeedsUsersRecordSample.SAMPLE)
            .addDataset(NewsRecordSamples.NewsFeedsRecordSample.SAMPLE)
            .addDataset(NewsRecordSamples.NewsUserStateSample.SAMPLE)
            .build();

    @RegisterExtension
    @SuppressWarnings("unused")
    static ChainedExtension chain = ChainedExtension.outer(wDs)
            .append(wBaywatchDb)
            .append(wDslContext)
            .append(wSamples)
            .register();

    private UserRepository tested;

    @BeforeEach
    void setUp(DSLContext dslContext) {
        BaywatchMapper baywatchMapper = Mappers.getMapper(BaywatchMapper.class);
        tested = new UserRepository(Schedulers.immediate(), dslContext, baywatchMapper);
    }

    @Test
    void should_get_user(WithSampleDataLoaded.Tracker dbTracker) {
        dbTracker.skipNextSampleLoad();

        User actual = tested.get(UsersRecordSamples.OKENOBI.getUserId()).block();

        assertThat(actual).isEqualTo(User.builder()
                .id("ad59d599c354564e682e20da2dd9a52b092bc69a87b1d0ca00300651b9abeb9f")
                .name("Obiwan Kenobi")
                .login("okenobi")
                .mail("obiwan.kenobi@jedi.fr")
                .build());
    }

    @Test
    void should_list_all_users(WithSampleDataLoaded.Tracker dbTracker) {
        dbTracker.skipNextSampleLoad();
        List<User> actuals = tested.list().collectList().block();

        assertThat(actuals).hasSize(2);
    }

    @Test
    void should_list_user_with_criteria(WithSampleDataLoaded.Tracker dbTracker) {
        dbTracker.skipNextSampleLoad();
        List<User> actuals = tested.list(PageRequest.all(Criteria.property("name").eq("Obiwan Kenobi"))).collectList().block();

        assertThat(actuals).isNotNull();
        assertThat(actuals).hasSize(1);
        assertThat(actuals.get(0).id).isEqualTo("ad59d599c354564e682e20da2dd9a52b092bc69a87b1d0ca00300651b9abeb9f");
    }

    @Test
    void should_persist_users(DSLContext dsl) {
        {
            int actual = dsl.fetchCount(Users.USERS);
            assertThat(actual).isEqualTo(2);
        }

        tested.persist(List.of(
                User.builder().id(Hasher.sha3("dvader")).login("dvader").name("Darth Vader").mail("darth.vader@sith.fr").build(),
                User.builder().id(Hasher.sha3("dsidious")).login("dsidious").name("Darth Sidious").mail("darth.sidious@sith.fr").build()
        )).collectList().block();

        {
            int actual = dsl.fetchCount(Users.USERS);
            assertThat(actual).isEqualTo(4);
        }
    }

    @Test
    void should_delete_users(DSLContext dsl) {
        Assertions.assertAll(
                () -> assertThat(dsl.fetchCount(Users.USERS)).isEqualTo(2),
                () -> assertThat(dsl.fetchCount(NEWS_USER_STATE)).isEqualTo(50),
                () -> assertThat(dsl.fetchCount(FEEDS_USERS)).isEqualTo(5)
        );

        tested.delete(List.of(UsersRecordSamples.OKENOBI.getUserId())).block();

        Assertions.assertAll(
                () -> assertThat(dsl.fetchCount(Users.USERS)).isEqualTo(1),
                () -> assertThat(dsl.fetchCount(NEWS_USER_STATE)).isEqualTo(25),
                () -> assertThat(dsl.fetchCount(FEEDS_USERS)).isEqualTo(2)
        );
    }
}