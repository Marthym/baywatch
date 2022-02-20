package fr.ght1pc9kc.baywatch.techwatch.infra.persistence;

import fr.ght1pc9kc.baywatch.common.api.model.Entity;
import fr.ght1pc9kc.baywatch.security.api.model.Role;
import fr.ght1pc9kc.baywatch.security.api.model.User;
import fr.ght1pc9kc.baywatch.security.domain.samples.UserSamples;
import fr.ght1pc9kc.baywatch.techwatch.domain.model.QueryContext;
import fr.ght1pc9kc.baywatch.common.domain.Hasher;
import fr.ght1pc9kc.baywatch.dsl.tables.Users;
import fr.ght1pc9kc.baywatch.common.infra.mappers.BaywatchMapper;
import fr.ght1pc9kc.baywatch.tests.samples.infra.FeedRecordSamples;
import fr.ght1pc9kc.baywatch.tests.samples.infra.FeedsUsersRecordSample;
import fr.ght1pc9kc.baywatch.tests.samples.infra.NewsRecordSamples;
import fr.ght1pc9kc.baywatch.tests.samples.infra.UsersRecordSamples;
import fr.ght1pc9kc.baywatch.security.infra.persistence.UserRepository;
import fr.ght1pc9kc.juery.api.Criteria;
import fr.irun.testy.core.extensions.ChainedExtension;
import fr.irun.testy.jooq.WithDatabaseLoaded;
import fr.irun.testy.jooq.WithDslContext;
import fr.irun.testy.jooq.WithInMemoryDatasource;
import fr.irun.testy.jooq.WithSampleDataLoaded;
import org.jooq.DSLContext;
import org.jooq.exception.DataAccessException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.mapstruct.factory.Mappers;
import reactor.core.scheduler.Schedulers;
import reactor.test.StepVerifier;

import java.time.Instant;
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

        StepVerifier.create(tested.get(UsersRecordSamples.OKENOBI.getUserId()))
                .assertNext(actual -> Assertions.assertAll(
                        () -> assertThat(actual.id).isEqualTo(UserSamples.OBIWAN.id),
                        () -> assertThat(actual.createdAt).isEqualTo(Instant.parse("1970-01-01T00:00:00Z")),
                        () -> assertThat(actual.entity.name).isEqualTo("Obiwan Kenobi"),
                        () -> assertThat(actual.entity.login).isEqualTo("okenobi"),
                        () -> assertThat(actual.entity.mail).isEqualTo("obiwan.kenobi@jedi.com"),
                        () -> assertThat(actual.entity.password).isEqualTo(UserSamples.OBIWAN.entity.password),
                        () -> assertThat(actual.entity.role).isEqualTo(Role.MANAGER)
                )).verifyComplete();
    }

    @Test
    void should_list_all_users(WithSampleDataLoaded.Tracker dbTracker) {
        dbTracker.skipNextSampleLoad();

        StepVerifier.create(tested.list())
                .expectNextCount(2)
                .verifyComplete();
    }

    @Test
    void should_list_user_with_criteria(WithSampleDataLoaded.Tracker dbTracker) {
        dbTracker.skipNextSampleLoad();

        StepVerifier.create(tested.list(QueryContext.all(Criteria.property("name").eq("Obiwan Kenobi"))))
                .expectNextMatches(actual -> UserSamples.OBIWAN.id.equals(actual.id))
                .verifyComplete();
    }

    @Test
    void should_persist_users(DSLContext dsl) {
        {
            int actual = dsl.fetchCount(Users.USERS);
            assertThat(actual).isEqualTo(2);
        }

        Entity<User> dvader = new Entity<>((Hasher.sha3("darth.vader@sith.fr")), Instant.EPOCH,
                User.builder().login("dvader").name("Darth Vader").mail("darth.vader@sith.fr").password("obscur").role(Role.USER).build());
        Entity<User> dsidious = new Entity<>((Hasher.sha3("darth.sidious@sith.fr")), Instant.EPOCH,
                User.builder().login("dsidious").name("Darth Sidious").mail("darth.sidious@sith.fr").password("obscur").role(Role.MANAGER).build());

        StepVerifier.create(
                        tested.persist(List.of(dvader, dsidious)))
                .expectNextCount(2)
                .verifyComplete();

        {
            int actual = dsl.fetchCount(Users.USERS);
            assertThat(actual).isEqualTo(4);
        }
    }

    @Test
    void should_persist_users_with_errors(DSLContext dsl) {
        {
            int actual = dsl.fetchCount(Users.USERS);
            assertThat(actual).isEqualTo(2);
        }

        Entity<User> dvader = new Entity<>((Hasher.sha3("darth.vader@sith.fr")), Instant.EPOCH,
                User.builder().login("dvader").name("Darth Vader").mail("darth.vader@sith.fr").password("obscur").role(Role.USER).build());
        StepVerifier.create(
                        tested.persist(List.of(dvader, UserSamples.LUKE, UserSamples.OBIWAN, UserSamples.YODA)))
                .verifyError(DataAccessException.class);

        {
            int actual = dsl.fetchCount(Users.USERS);
            assertThat(actual).isEqualTo(2);
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