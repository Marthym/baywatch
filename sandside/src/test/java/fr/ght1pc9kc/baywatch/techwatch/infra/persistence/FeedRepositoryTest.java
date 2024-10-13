package fr.ght1pc9kc.baywatch.techwatch.infra.persistence;

import fr.ght1pc9kc.baywatch.common.api.model.FeedMeta;
import fr.ght1pc9kc.baywatch.common.domain.Hasher;
import fr.ght1pc9kc.baywatch.common.domain.QueryContext;
import fr.ght1pc9kc.baywatch.dsl.tables.records.FeedsRecord;
import fr.ght1pc9kc.baywatch.dsl.tables.records.FeedsUsersPropertiesRecord;
import fr.ght1pc9kc.baywatch.dsl.tables.records.FeedsUsersRecord;
import fr.ght1pc9kc.baywatch.techwatch.api.model.WebFeed;
import fr.ght1pc9kc.baywatch.techwatch.infra.config.TechwatchMapper;
import fr.ght1pc9kc.baywatch.techwatch.infra.model.FeedDeletedResult;
import fr.ght1pc9kc.baywatch.techwatch.infra.model.FeedProperties;
import fr.ght1pc9kc.baywatch.tests.samples.infra.FeedRecordSamples;
import fr.ght1pc9kc.baywatch.tests.samples.infra.FeedsUsersPropertiesRecordSample;
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
import org.apache.commons.lang3.stream.Streams;
import org.assertj.core.api.Assertions;
import org.assertj.core.api.SoftAssertions;
import org.jooq.DSLContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.mapstruct.factory.Mappers;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import reactor.test.StepVerifier;

import java.net.URI;
import java.time.Instant;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static fr.ght1pc9kc.baywatch.common.api.model.EntitiesProperties.COUNT;
import static fr.ght1pc9kc.baywatch.common.api.model.EntitiesProperties.FEED_ID;
import static fr.ght1pc9kc.baywatch.common.api.model.FeedMeta.updated;
import static fr.ght1pc9kc.baywatch.dsl.tables.Feeds.FEEDS;
import static fr.ght1pc9kc.baywatch.dsl.tables.FeedsUsers.FEEDS_USERS;
import static fr.ght1pc9kc.baywatch.dsl.tables.FeedsUsersProperties.FEEDS_USERS_PROPERTIES;
import static fr.ght1pc9kc.baywatch.tests.samples.UserSamples.OBIWAN;
import static fr.ght1pc9kc.baywatch.tests.samples.infra.UsersRecordSamples.LSKYWALKER;
import static fr.ght1pc9kc.baywatch.tests.samples.infra.UsersRecordSamples.OKENOBI;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

class FeedRepositoryTest {
    private static final WithInMemoryDatasource wDs = WithInMemoryDatasource.builder().build();
    private static final WithDslContext wDslContext = WithDslContext.builder()
            .setDatasourceExtension(wDs).build();
    private static final WithSampleDataLoaded wSamples = WithSampleDataLoaded.builder(wDslContext)
            .createTablesIfNotExists()
            .addDataset(FeedRecordSamples.SAMPLE)
            .addDataset(UsersRecordSamples.SAMPLE)
            .addDataset(UsersRolesSamples.SAMPLE)
            .addDataset(NewsRecordSamples.SAMPLE)
            .addDataset(NewsRecordSamples.NewsFeedsRecordSample.SAMPLE)
            .addDataset(FeedsUsersRecordSample.SAMPLE)
            .addDataset(FeedsUsersPropertiesRecordSample.SAMPLE)
            .build();

    @RegisterExtension
    @SuppressWarnings("unused")
    static ChainedExtension chain = ChainedExtension.outer(wDs)
            .append(wDslContext)
            .append(wSamples)
            .register();

    private FeedRepository tested;

    @BeforeEach
    void setUp(DSLContext dslContext) {
        TechwatchMapper mapper = Mappers.getMapper(TechwatchMapper.class);
        tested = new FeedRepository(Schedulers.immediate(), dslContext, mapper);
    }

    @Test
    void should_get_user_feed(WithSampleDataLoaded.Tracker tracker) {
        tracker.skipNextSampleLoad();
        FeedsRecord expected = FeedRecordSamples.SAMPLE.records().getFirst();
        Entity<WebFeed> actual = tested.get(QueryContext.id(expected.getFeedId())).block();

        assertThat(actual).isNotNull();
        assertAll(
                () -> assertThat(actual.id()).isEqualTo(expected.getFeedId()),
                () -> assertThat(actual.self().location()).isEqualTo(URI.create(expected.getFeedUrl())),
                () -> assertThat(actual.self().name()).isEqualTo(expected.getFeedName()),
                () -> assertThat(actual.self().tags()).isEqualTo(Set.of())
        );

    }

    @Test
    void should_list_all_feeds(WithSampleDataLoaded.Tracker dbTracker) {
        dbTracker.skipNextSampleLoad();
        List<Entity<WebFeed>> actuals = tested.list().collectList().block();
        assertThat(actuals).isNotEmpty();
    }

    @Test
    void should_manage_backpressure(WithSampleDataLoaded.Tracker dbTracker) {
        dbTracker.skipNextSampleLoad();
        List<Entity<WebFeed>> actuals = tested.list().limitRate(2).collectList().block();
        assertThat(actuals).hasSize(FeedRecordSamples.SAMPLE.records().size());
    }

    @Test
    void should_persist_feeds(DSLContext dsl) {
        URI uri = URI.create("https://obiwan.kenobi.jedi/.rss");
        String reference = Hasher.identify(uri);
        Entity<WebFeed> expected = Entity.identify(WebFeed.builder()
                        .location(uri)
                        .name("Obiwan Kenobi")
                        .tags(Set.of())
                        .build())
                .meta(updated, Instant.EPOCH)
                .withId(reference);

        StepVerifier.create(tested.persist(Collections.singleton(expected)))
                .assertNext(actual -> assertThat(actual).isEqualTo(expected))
                .verifyComplete();

        FeedsRecord actual = dsl.selectFrom(FEEDS).where(FEEDS.FEED_ID.eq(expected.id())).fetchOne();
        assertThat(actual).isNotNull();
        assertThat(actual.getFeedName()).isEqualTo(expected.self().name());
    }

    @Test
    void should_persist_feeds_to_user(DSLContext dsl) {
        Entity<WebFeed> expected = Entity.identify(
                        Mappers.getMapper(TechwatchMapper.class).recordToFeed(FeedRecordSamples.JEDI)
                                .self().toBuilder()
                                .name(FeedRecordSamples.JEDI.getFeedName())
                                .tags(Set.of())
                                .build())
                .meta(updated, Instant.parse("2020-12-11T15:12:42Z"))
                .withId(FeedRecordSamples.JEDI.getFeedId());


        StepVerifier.create(tested.persistUserRelation(OKENOBI.getUserId(), Collections.singleton(expected)))
                .assertNext(actual -> assertThat(actual).isEqualTo(expected))
                .verifyComplete();

        {
            FeedsRecord actual = dsl.selectFrom(FEEDS).where(FEEDS.FEED_ID.eq(expected.id())).fetchOne();
            assertThat(actual).isNotNull();
            assertThat(actual.getFeedName()).isEqualTo(FeedRecordSamples.JEDI.getFeedName());
        }
        {
            FeedsUsersRecord actual = dsl.selectFrom(FEEDS_USERS).where(FEEDS_USERS.FEUS_FEED_ID.eq(expected.id()))
                    .fetchOne();
            assertThat(actual).isNotNull();
            assertThat(actual.getFeusUserId()).isEqualTo(OKENOBI.getUserId());
        }
    }

    @Test
    void should_partially_update_feed(DSLContext dsl) {
        String feedOwnedOnlyByObywan = Hasher.identify(FeedRecordSamples.JEDI_BASE_URI.resolve("01"));
        SoftAssertions.assertSoftly(softly -> {
            FeedsRecord initial = dsl.selectFrom(FEEDS).where(FEEDS.FEED_ID.eq(feedOwnedOnlyByObywan)).fetchOne();
            softly.assertThat(initial).isNotNull();
            assert initial != null;
            softly.assertThat(initial.getFeedName()).isEqualTo("Jedi");
            softly.assertThat(initial.getFeedDescription()).isEqualTo("Feed description");
            softly.assertThat(initial.getFeedLastEtag()).isNull();
        });

        Entity<WebFeed> expected = Entity.identify(
                        WebFeed.builder()
                                .location(URI.create("http://www.jedi.light/01"))
                                .tags(Set.of())
                                .build())
                .meta(FeedMeta.ETag, "updatedETag")
                .withId(feedOwnedOnlyByObywan);

        StepVerifier.create(tested.update(List.of(expected)))
                .assertNext(actual -> assertThat(actual.meta(FeedMeta.ETag)).contains("updatedETag"))
                .verifyComplete();

        SoftAssertions.assertSoftly(softly -> {
            FeedsRecord after = dsl.selectFrom(FEEDS).where(FEEDS.FEED_ID.eq(feedOwnedOnlyByObywan)).fetchOne();
            softly.assertThat(after).isNotNull();
            assert after != null;
            softly.assertThat(after.getFeedName()).isEqualTo("Jedi");
            softly.assertThat(after.getFeedDescription()).isEqualTo("Feed description");
            softly.assertThat(after.getFeedLastEtag()).isEqualTo("updatedETag");
        });
    }

    @Test
    void should_delete_feeds(DSLContext dsl) {
        String feedOwnedByObiwanAndSkywalker = Hasher.identify(FeedRecordSamples.JEDI_BASE_URI.resolve("01"));
        String feedOwnedOnlyByObywan = Hasher.identify(FeedRecordSamples.JEDI_BASE_URI.resolve("03"));
        List<String> ids = List.of(feedOwnedByObiwanAndSkywalker, feedOwnedOnlyByObywan);

        {
            int countUser = dsl.fetchCount(FEEDS_USERS, FEEDS_USERS.FEUS_FEED_ID.in(ids));
            assertThat(countUser).isEqualTo(3);
        }

        tested.delete(QueryContext.all(Criteria.property(FEED_ID).in(ids))).block();

        {
            int countUser = dsl.fetchCount(FEEDS_USERS, FEEDS_USERS.FEUS_FEED_ID.in(ids));
            assertThat(countUser).isZero();
        }
    }

    @Test
    void should_delete_without_filter() {
        Mono<FeedDeletedResult> actual = tested.delete(QueryContext.builder()
                .filter(Criteria.property(FEED_ID).in("1", "2"))
                .userId(OKENOBI.getUserId())
                .build());

        StepVerifier.create(actual)
                .expectNext(new FeedDeletedResult(0, 0))
                .verifyComplete();
    }

    @Test
    void should_delete_feeds_for_user(DSLContext dsl) {
        String feedOwnedByObiwanAndSkywalker = Hasher.identify(FeedRecordSamples.JEDI_BASE_URI.resolve("01"));
        String feedOwnedOnlyByObywan = Hasher.identify(FeedRecordSamples.JEDI_BASE_URI.resolve("02"));
        List<String> ids = List.of(feedOwnedByObiwanAndSkywalker, feedOwnedOnlyByObywan);

        {
            int countUser = dsl.fetchCount(FEEDS_USERS, FEEDS_USERS.FEUS_FEED_ID.in(ids)
                    .and(FEEDS_USERS.FEUS_USER_ID.eq(OBIWAN.id())));
            assertThat(countUser).isEqualTo(2);
        }

        tested.delete(QueryContext.builder()
                .filter(Criteria.property(FEED_ID).in(ids))
                .userId(OBIWAN.id())
                .build()).block();

        {
            int countUser = dsl.fetchCount(FEEDS_USERS, FEEDS_USERS.FEUS_FEED_ID.in(ids));
            assertThat(countUser)
                    .describedAs("Feed 02 was remove, feed 01, owned by LSKYWALKER was kept")
                    .isEqualTo(1);
        }
    }

    @Test
    void should_list_orphan_feed(WithSampleDataLoaded.Tracker tracker) {
        tracker.skipNextSampleLoad();
        List<Entity<WebFeed>> actuals = tested.list(QueryContext.all(Criteria.property(COUNT).eq(0))).collectList().block();
        assertThat(actuals).extracting(Entity::id).containsExactly(
                FeedRecordSamples.FEEDS_RECORDS.getLast().getFeedId());
    }

    @Test
    void should_get_feed_user_properties(WithSampleDataLoaded.Tracker tracker) {
        tracker.skipNextSampleLoad();
        StepVerifier.create(tested.getFeedProperties(
                        OKENOBI.getUserId(),
                        List.of(FeedRecordSamples.JEDI.getFeedId()),
                        EnumSet.of(FeedProperties.NAME)
                ))
                .assertNext(actual -> Assertions.assertThat(actual).isEqualTo(Entity.identify(
                                Map.of(FeedProperties.NAME, "Customized for Obiwan JEDI Name"))
                        .meta(FeedMeta.createdBy, OKENOBI.getUserId())
                        .withId(FeedRecordSamples.JEDI.getFeedId())
                ))
                .verifyComplete();

        StepVerifier.create(tested.getFeedProperties(
                        OKENOBI.getUserId(),
                        List.of(FeedRecordSamples.JEDI.getFeedId()),
                        null
                ))
                .assertNext(actual -> Assertions.assertThat(actual).isEqualTo(Entity.identify(
                                Map.of(
                                        FeedProperties.NAME, "Customized for Obiwan JEDI Name",
                                        FeedProperties.TAG, "light,republic"
                                ))
                        .meta(FeedMeta.createdBy, OKENOBI.getUserId())
                        .withId(FeedRecordSamples.JEDI.getFeedId())
                ))
                .verifyComplete();

        StepVerifier.create(tested.getFeedProperties(
                        OKENOBI.getUserId(),
                        List.of(FeedRecordSamples.JEDI.getFeedId()),
                        EnumSet.of(FeedProperties.NAME, FeedProperties.TAG)
                ))
                .assertNext(actual -> Assertions.assertThat(actual).isEqualTo(Entity.identify(
                                Map.of(
                                        FeedProperties.NAME, "Customized for Obiwan JEDI Name",
                                        FeedProperties.TAG, "light,republic"
                                ))
                        .meta(FeedMeta.createdBy, OKENOBI.getUserId())
                        .withId(FeedRecordSamples.JEDI.getFeedId())
                ))
                .verifyComplete();

        StepVerifier.create(tested.getFeedProperties(
                        OKENOBI.getUserId(),
                        List.of(FeedRecordSamples.JEDI.getFeedId()),
                        EnumSet.noneOf(FeedProperties.class)
                ))
                .assertNext(actual -> Assertions.assertThat(actual).isEqualTo(Entity.identify(
                                Map.of(
                                        FeedProperties.NAME, "Customized for Obiwan JEDI Name",
                                        FeedProperties.TAG, "light,republic"
                                ))
                        .meta(FeedMeta.createdBy, OKENOBI.getUserId())
                        .withId(FeedRecordSamples.JEDI.getFeedId())
                ))
                .verifyComplete();

        StepVerifier.create(tested.getFeedProperties(
                        OKENOBI.getUserId(),
                        List.of(FeedRecordSamples.JEDI.getFeedId(), "42"),
                        EnumSet.of(FeedProperties.NAME)
                ))
                .assertNext(actual -> Assertions.assertThat(actual).isEqualTo(Entity.identify(
                                Map.of(FeedProperties.NAME, "Customized for Obiwan JEDI Name"))
                        .meta(FeedMeta.createdBy, OKENOBI.getUserId())
                        .withId(FeedRecordSamples.JEDI.getFeedId())
                ))
                .verifyComplete();

        StepVerifier.create(tested.getFeedProperties(
                OKENOBI.getUserId(),
                List.of(),
                EnumSet.of(FeedProperties.NAME)
        )).verifyError(IllegalArgumentException.class);
    }

    @Test
    void should_set_feed_user_properties(DSLContext dslContext) {
        FeedsUsersPropertiesRecord beforeRecord = dslContext.selectFrom(FEEDS_USERS_PROPERTIES).where(
                FEEDS_USERS_PROPERTIES.FUPR_FEED_ID.eq(FeedRecordSamples.JEDI.getFeedId()),
                FEEDS_USERS_PROPERTIES.FUPR_USER_ID.eq(LSKYWALKER.getUserId())
        ).fetchOne();

        Assertions.assertThat(beforeRecord).isNull();

        StepVerifier.create(tested.setFeedProperties(LSKYWALKER.getUserId(), List.of(
                Entity.identify(WebFeed.builder()
                                .location(URI.create("https://jedi.com"))
                                .name("Customized for Obiwan JEDI Name")
                                .description("Customized for Obiwan JEDI Description")
                                .tags(Set.of("light", "good"))
                                .build())
                        .withId(FeedRecordSamples.JEDI.getFeedId())
        ))).verifyComplete();

        FeedsUsersPropertiesRecord[] actualRecord = dslContext.selectFrom(FEEDS_USERS_PROPERTIES).where(
                FEEDS_USERS_PROPERTIES.FUPR_FEED_ID.eq(FeedRecordSamples.JEDI.getFeedId()),
                FEEDS_USERS_PROPERTIES.FUPR_USER_ID.eq(LSKYWALKER.getUserId())
        ).fetchArray();

        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(actualRecord)
                    .isNotNull()
                    .hasSize(4);

            softly.assertThat(Streams.of(actualRecord).map(FeedsUsersPropertiesRecord::getFuprPropertyName))
                    .containsExactly(
                            FeedProperties.NAME.name(),
                            FeedProperties.DESCRIPTION.name(),
                            FeedProperties.TAG.name(),
                            FeedProperties.TAG.name()
                    );

            softly.assertThat(Streams.of(actualRecord).map(FeedsUsersPropertiesRecord::getFuprPropertyValue))
                    .containsExactlyInAnyOrder(
                            "Customized for Obiwan JEDI Name",
                            "Customized for Obiwan JEDI Description",
                            "light",
                            "good"
                    );
        });

    }

    @Test
    void should_delete_feed_user_properties(DSLContext dslContext) {
        int initialCount = dslContext.fetchCount(FEEDS_USERS_PROPERTIES,
                FEEDS_USERS_PROPERTIES.FUPR_FEED_ID.eq(FeedRecordSamples.JEDI.getFeedId()),
                FEEDS_USERS_PROPERTIES.FUPR_USER_ID.eq(OKENOBI.getUserId())
        );

        Assertions.assertThat(initialCount).isNotZero();

        StepVerifier.create(tested.deleteFeedProperties(
                        OKENOBI.getUserId(), List.of(FeedRecordSamples.JEDI.getFeedId())))
                .verifyComplete();

        int actualCount = dslContext.fetchCount(FEEDS_USERS_PROPERTIES,
                FEEDS_USERS_PROPERTIES.FUPR_FEED_ID.eq(FeedRecordSamples.JEDI.getFeedId()),
                FEEDS_USERS_PROPERTIES.FUPR_USER_ID.eq(OKENOBI.getUserId())
        );
        Assertions.assertThat(actualCount).isZero();
    }

    @Test
    void should_delete_feed_user_relations(DSLContext dslContext) {
        int initialCount = dslContext.fetchCount(FEEDS_USERS,
                FEEDS_USERS.FEUS_FEED_ID.eq(FeedRecordSamples.JEDI.getFeedId()),
                FEEDS_USERS.FEUS_USER_ID.eq(OKENOBI.getUserId())
        );

        Assertions.assertThat(initialCount).isNotZero();

        StepVerifier.create(tested.deleteUserRelations(
                        OKENOBI.getUserId(), List.of(FeedRecordSamples.JEDI.getFeedId())))
                .verifyComplete();

        int actualCount = dslContext.fetchCount(FEEDS_USERS,
                FEEDS_USERS.FEUS_FEED_ID.eq(FeedRecordSamples.JEDI.getFeedId()),
                FEEDS_USERS.FEUS_USER_ID.eq(OKENOBI.getUserId())
        );
        Assertions.assertThat(actualCount).isZero();
    }
}
