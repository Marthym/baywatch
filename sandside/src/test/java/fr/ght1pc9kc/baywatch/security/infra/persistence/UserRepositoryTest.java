package fr.ght1pc9kc.baywatch.security.infra.persistence;

import fr.ght1pc9kc.baywatch.common.domain.Hasher;
import fr.ght1pc9kc.baywatch.common.domain.QueryContext;
import fr.ght1pc9kc.baywatch.dsl.tables.Users;
import fr.ght1pc9kc.baywatch.security.api.model.Permission;
import fr.ght1pc9kc.baywatch.security.api.model.Role;
import fr.ght1pc9kc.baywatch.security.api.model.User;
import fr.ght1pc9kc.baywatch.security.domain.exceptions.ConstraintViolationPersistenceException;
import fr.ght1pc9kc.baywatch.security.infra.adapters.UserMapper;
import fr.ght1pc9kc.baywatch.tests.samples.UserSamples;
import fr.ght1pc9kc.baywatch.tests.samples.infra.FeedRecordSamples;
import fr.ght1pc9kc.baywatch.tests.samples.infra.FeedsUsersRecordSample;
import fr.ght1pc9kc.baywatch.tests.samples.infra.NewsRecordSamples;
import fr.ght1pc9kc.baywatch.tests.samples.infra.UsersRecordSamples;
import fr.ght1pc9kc.baywatch.tests.samples.infra.UsersRolesSamples;
import fr.ght1pc9kc.entity.api.Entity;
import fr.ght1pc9kc.juery.api.Criteria;
import fr.ght1pc9kc.testy.core.extensions.ChainedExtension;
import fr.ght1pc9kc.testy.jooq.WithDslContext;
import fr.ght1pc9kc.testy.jooq.WithInMemoryDatasource;
import fr.ght1pc9kc.testy.jooq.WithSampleDataLoaded;
import org.assertj.core.api.SoftAssertions;
import org.jooq.DSLContext;
import org.jooq.TransactionalCallable;
import org.jooq.exception.DataAccessException;
import org.jooq.exception.IntegrityConstraintViolationException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.mapstruct.factory.Mappers;
import reactor.core.scheduler.Schedulers;
import reactor.test.StepVerifier;

import java.time.Instant;
import java.util.List;

import static fr.ght1pc9kc.baywatch.common.api.model.UserMeta.createdAt;
import static fr.ght1pc9kc.baywatch.dsl.tables.FeedsUsers.FEEDS_USERS;
import static fr.ght1pc9kc.baywatch.dsl.tables.NewsUserState.NEWS_USER_STATE;
import static fr.ght1pc9kc.baywatch.dsl.tables.UsersRoles.USERS_ROLES;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.spy;

class UserRepositoryTest {
    private static final WithInMemoryDatasource wDs = WithInMemoryDatasource.builder().build();
    private static final WithDslContext wDslContext = WithDslContext.builder()
            .setDatasourceExtension(wDs).build();
    private static final WithSampleDataLoaded wSamples = WithSampleDataLoaded.builder(wDslContext)
            .createTablesIfNotExists()
            .addDataset(UsersRecordSamples.SAMPLE)
            .addDataset(UsersRolesSamples.SAMPLE)
            .addDataset(FeedRecordSamples.SAMPLE)
            .addDataset(NewsRecordSamples.SAMPLE)
            .addDataset(FeedsUsersRecordSample.SAMPLE)
            .addDataset(NewsRecordSamples.NewsFeedsRecordSample.SAMPLE)
            .addDataset(NewsRecordSamples.NewsUserStateSample.SAMPLE)
            .build();

    @RegisterExtension
    @SuppressWarnings("unused")
    static ChainedExtension chain = ChainedExtension.outer(wDs)
            .append(wDslContext)
            .append(wSamples)
            .register();

    private UserRepository tested;

    @BeforeEach
    void setUp(DSLContext dslContext) {
        UserMapper userMapper = Mappers.getMapper(UserMapper.class);
        tested = new UserRepository(Schedulers.immediate(), dslContext, userMapper);
    }

    @Test
    void should_get_user(WithSampleDataLoaded.Tracker dbTracker) {
        dbTracker.skipNextSampleLoad();

        StepVerifier.create(tested.get(UsersRecordSamples.OKENOBI.getUserId()))
                .assertNext(actual -> Assertions.assertAll(
                        () -> assertThat(actual.id()).isEqualTo(UserSamples.OBIWAN.id()),
                        () -> assertThat(actual.meta(createdAt, Instant.class)).isPresent().contains(Instant.parse("1970-01-01T00:00:00Z")),
                        () -> assertThat(actual.self().name()).isEqualTo("Obiwan Kenobi"),
                        () -> assertThat(actual.self().login()).isEqualTo("okenobi"),
                        () -> assertThat(actual.self().mail()).isEqualTo("obiwan.kenobi@jedi.com"),
                        () -> assertThat(actual.self().password()).isEqualTo(UserSamples.OBIWAN.self().password()),
                        () -> assertThat(actual.self().roles()).containsOnly(Role.MANAGER)
                )).verifyComplete();
    }

    @Test
    void should_get_user_with_multiple_roles(WithSampleDataLoaded.Tracker dbTracker) {
        dbTracker.skipNextSampleLoad();

        StepVerifier.create(tested.get(UsersRecordSamples.LSKYWALKER.getUserId()))
                .assertNext(actual -> Assertions.assertAll(
                        () -> assertThat(actual.id()).isEqualTo(UserSamples.LUKE.id()),
                        () -> assertThat(actual.meta(createdAt, Instant.class)).isPresent().contains(Instant.parse("1970-01-01T00:00:00Z")),
                        () -> assertThat(actual.self().name()).isEqualTo("Luke Skywalker"),
                        () -> assertThat(actual.self().login()).isEqualTo("lskywalker"),
                        () -> assertThat(actual.self().mail()).isEqualTo("luke.skywalker@jedi.com"),
                        () -> assertThat(actual.self().password()).isEqualTo(UserSamples.LUKE.self().password()),
                        () -> assertThat(actual.self().roles()).containsOnly(Role.USER, Permission.manager("TM01GP696RFPTY32WD79CVB0KDTF"))
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
    void should_count_all_users(WithSampleDataLoaded.Tracker dbTracker) {
        dbTracker.skipNextSampleLoad();

        StepVerifier.create(tested.count(QueryContext.all(Criteria.none())))
                .expectNextCount(1)
                .verifyComplete();
    }

    @Test
    void should_list_user_with_criteria(WithSampleDataLoaded.Tracker dbTracker) {
        dbTracker.skipNextSampleLoad();

        StepVerifier.create(tested.list(QueryContext.all(Criteria.property("name").eq("Obiwan Kenobi"))))
                .expectNextMatches(actual -> UserSamples.OBIWAN.id().equals(actual.id()))
                .verifyComplete();
    }

    @Test
    void should_persist_users(DSLContext dsl) {
        {
            var actual = List.of(dsl.fetchCount(Users.USERS), dsl.fetchCount(USERS_ROLES));
            assertThat(actual).containsExactly(2, 3);
        }

        Entity<User> dvader = Entity.identify(User.builder()
                        .login("dvader").name("Darth Vader").mail("darth.vader@sith.fr").password("obscur")
                        .role(Role.USER)
                        .role(Permission.manager("TM01GP696RFPTY32WD79CVB0KDTF")).build())
                .withId("US01GRQ15FNVC3XB8F1DD61J7WCC");

        Entity<User> dsidious = Entity.identify(User.builder()
                        .login("dsidious").name("Darth Sidious").mail("darth.sidious@sith.fr").password("obscur")
                        .role(Role.MANAGER).build())
                .withId("US01GRQ15G42PH15Q4RTKGN8KBT4");

        StepVerifier.create(
                        tested.persist(List.of(dvader, dsidious)))
                .expectNextCount(2)
                .verifyComplete();

        {
            var actual = List.of(dsl.fetchCount(Users.USERS), dsl.fetchCount(USERS_ROLES));
            assertThat(actual)
                    .describedAs("function must persist only user not roles")
                    .containsExactly(4, 3);
        }
    }

    @Test
    void should_persist_users_with_errors(DSLContext dsl) {
        {
            int actual = dsl.fetchCount(Users.USERS);
            assertThat(actual).isEqualTo(2);
        }

        Entity<User> dvader = Entity.identify(User.builder()
                        .login("dvader").name("Darth Vader").mail("darth.vader@sith.fr").password("obscur")
                        .role(Role.USER).build())
                .withId(Hasher.sha3("darth.vader@sith.fr"));

        StepVerifier.create(
                        tested.persist(List.of(dvader, UserSamples.LUKE, UserSamples.OBIWAN, UserSamples.YODA)))
                .verifyError(DataAccessException.class);

        {
            int actual = dsl.fetchCount(Users.USERS);
            assertThat(actual).isEqualTo(2);
        }
    }

    @Test
    void should_persist_users_with_constraint_errors(DSLContext dsl) {
        {
            int actual = dsl.fetchCount(Users.USERS);
            assertThat(actual).isEqualTo(2);
        }

        DSLContext dslContext = spy(dsl);
        doThrow(new IntegrityConstraintViolationException("")).when(dslContext).transactionResult(any(TransactionalCallable.class));
        tested = new UserRepository(Schedulers.immediate(), dslContext, Mappers.getMapper(UserMapper.class));

        Entity<User> dvader = Entity.identify(User.builder()
                        .login(UserSamples.LUKE.self().login()).name("Darth Vader").mail("darth.vader@sith.fr").password("obscur")
                        .role(Role.USER).build())
                .withId(Hasher.sha3("darth.vader@sith.fr"));

        StepVerifier.create(tested.persist(List.of(dvader)))
                .verifyError(ConstraintViolationPersistenceException.class);

        {
            int actual = dsl.fetchCount(Users.USERS);
            assertThat(actual).isEqualTo(2);
        }
    }

    @Test
    void should_update_users(DSLContext dsl) {
        {
            var actual = dsl.select(Users.USERS.USER_NAME).from(Users.USERS).where(Users.USERS.USER_ID.eq(UserSamples.OBIWAN.id()))
                    .fetchOne(Users.USERS.USER_NAME);
            assertThat(actual).isEqualTo("Obiwan Kenobi");
        }

        StepVerifier.create(tested.update(UserSamples.OBIWAN.convert(obi -> obi.toBuilder().name("Master Kenobi").build())))
                .assertNext(actual -> SoftAssertions.assertSoftly(soft -> {
                    soft.assertThat(actual.id()).isEqualTo(UserSamples.OBIWAN.id());
                    soft.assertThat(actual.self().name()).isEqualTo("Master Kenobi");
                }))
                .verifyComplete();

        {
            var actual = dsl.select(Users.USERS.USER_NAME).from(Users.USERS).where(Users.USERS.USER_ID.eq(UserSamples.OBIWAN.id()))
                    .fetchOne(Users.USERS.USER_NAME);
            assertThat(actual).isEqualTo("Master Kenobi");
        }
    }

    @Test
    void should_delete_users(DSLContext dsl) {
        Assertions.assertAll(
                () -> assertThat(dsl.fetchCount(Users.USERS)).isEqualTo(2),
                () -> assertThat(dsl.fetchCount(NEWS_USER_STATE)).isEqualTo(50),
                () -> assertThat(dsl.fetchCount(FEEDS_USERS)).isEqualTo(6)
        );

        tested.delete(List.of(UsersRecordSamples.OKENOBI.getUserId())).block();

        Assertions.assertAll(
                () -> assertThat(dsl.fetchCount(Users.USERS)).isEqualTo(1),
                () -> assertThat(dsl.fetchCount(NEWS_USER_STATE)).isEqualTo(25),
                () -> assertThat(dsl.fetchCount(FEEDS_USERS)).isEqualTo(2)
        );
    }

    @Test
    void should_persist_roles(DSLContext dsl) {
        assertThat(
                dsl.select().from(USERS_ROLES)
                        .where(USERS_ROLES.USRO_USER_ID.eq(UsersRecordSamples.LSKYWALKER.getUserId()))
                        .fetch(USERS_ROLES.USRO_ROLE))
                .containsOnly("MANAGER:TM01GP696RFPTY32WD79CVB0KDTF", "USER");

        StepVerifier.create(tested.persist(UsersRecordSamples.LSKYWALKER.getUserId(), List.of("MANAGER:42"))
                        .map(Entity::self))
                .expectNext(UserSamples.LUKE.self().withRoles("MANAGER:TM01GP696RFPTY32WD79CVB0KDTF", "USER", "MANAGER:42"))
                .verifyComplete();

        assertThat(
                dsl.select().from(USERS_ROLES)
                        .where(USERS_ROLES.USRO_USER_ID.eq(UsersRecordSamples.LSKYWALKER.getUserId()))
                        .fetch(USERS_ROLES.USRO_ROLE))
                .containsOnly("MANAGER:TM01GP696RFPTY32WD79CVB0KDTF", "USER", "MANAGER:42");
    }

    @Test
    void should_delete_roles(DSLContext dsl) {
        assertThat(
                dsl.select().from(USERS_ROLES)
                        .where(USERS_ROLES.USRO_USER_ID.eq(UsersRecordSamples.LSKYWALKER.getUserId()))
                        .fetch(USERS_ROLES.USRO_ROLE))
                .containsOnly("MANAGER:TM01GP696RFPTY32WD79CVB0KDTF", "USER");

        StepVerifier.create(tested.delete("MANAGER:TM01GP696RFPTY32WD79CVB0KDTF", List.of(UsersRecordSamples.LSKYWALKER.getUserId())))
                .expectNext()
                .verifyComplete();

        assertThat(
                dsl.select().from(USERS_ROLES)
                        .where(USERS_ROLES.USRO_USER_ID.eq(UsersRecordSamples.LSKYWALKER.getUserId()))
                        .fetch(USERS_ROLES.USRO_ROLE))
                .containsOnly("USER");
    }
}