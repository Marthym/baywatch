package fr.ght1pc9kc.baywatch.techwatch.infra.persistence;

import fr.ght1pc9kc.baywatch.common.domain.Hasher;
import fr.ght1pc9kc.baywatch.common.infra.mappers.BaywatchMapper;
import fr.ght1pc9kc.baywatch.dsl.tables.records.FeedsRecord;
import fr.ght1pc9kc.baywatch.dsl.tables.records.FeedsUsersRecord;
import fr.ght1pc9kc.baywatch.techwatch.api.model.WebFeed;
import fr.ght1pc9kc.baywatch.techwatch.domain.model.QueryContext;
import fr.ght1pc9kc.baywatch.techwatch.infra.model.FeedDeletedResult;
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
import org.jooq.DSLContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.mapstruct.factory.Mappers;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import reactor.test.StepVerifier;

import java.net.URI;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import static fr.ght1pc9kc.baywatch.common.api.model.EntitiesProperties.COUNT;
import static fr.ght1pc9kc.baywatch.common.api.model.EntitiesProperties.FEED_ID;
import static fr.ght1pc9kc.baywatch.dsl.tables.Feeds.FEEDS;
import static fr.ght1pc9kc.baywatch.dsl.tables.FeedsUsers.FEEDS_USERS;
import static fr.ght1pc9kc.baywatch.tests.samples.UserSamples.OBIWAN;
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
        BaywatchMapper baywatchMapper = Mappers.getMapper(BaywatchMapper.class);
        tested = new FeedRepository(Schedulers.immediate(), dslContext, baywatchMapper);
    }

    @Test
    void should_get_user_feed() {
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
        WebFeed expected = WebFeed.builder()
                .reference(Hasher.identify(uri))
                .location(uri)
                .name("Obiwan Kenobi")
                .tags(Set.of())
                .build();

        StepVerifier.create(tested.persist(Collections.singleton(expected)))
                .assertNext(actual -> assertThat(actual).isEqualTo(Entity.identify(expected).withId(expected.reference())))
                .verifyComplete();

        FeedsRecord actual = dsl.selectFrom(FEEDS).where(FEEDS.FEED_ID.eq(expected.reference())).fetchOne();
        assertThat(actual).isNotNull();
        assertThat(actual.getFeedName()).isEqualTo(expected.name());
    }

    @Test
    void should_persist_feeds_to_user(DSLContext dsl) {
        Entity<WebFeed> expected = Entity.identify(
                Mappers.getMapper(BaywatchMapper.class).recordToFeed(FeedRecordSamples.JEDI)
                        .self().toBuilder()
                        .name(FeedRecordSamples.JEDI.getFeedName() + " of Obiwan")
                        .tags(Set.of("jedi", "saber"))
                        .build()
        ).withId(FeedRecordSamples.JEDI.getFeedId());


        StepVerifier.create(tested.persistUserRelation(Collections.singleton(expected.self()), OKENOBI.getUserId()))
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
            assertThat(actual.getFeusTags()).isEqualTo(String.join(",", expected.self().tags()));
        }
    }

    @Test
    void should_update_feed(DSLContext dsl) {
        String feedOwnedOnlyByObywan = Hasher.identify(FeedRecordSamples.JEDI_BASE_URI.resolve("01"));
        Entity<WebFeed> expected = Entity.identify(
                WebFeed.builder()
                        .reference(feedOwnedOnlyByObywan)
                        .location(URI.create("http://www.jedi.light/01"))
                        .name("Obiwan Kenobi")
                        .tags(Set.of("jedi", "light"))
                        .build()
        ).withId(feedOwnedOnlyByObywan);
        Mono<Entity<WebFeed>> update = tested.update(expected.id(), OKENOBI.getUserId(), expected.self());
        StepVerifier.create(update)
                .expectNext(expected)
                .verifyComplete();

        {
            FeedsUsersRecord actual = dsl.selectFrom(FEEDS_USERS).where(
                            FEEDS_USERS.FEUS_USER_ID.eq(OKENOBI.getUserId())
                                    .and(FEEDS_USERS.FEUS_FEED_ID.eq(feedOwnedOnlyByObywan)))
                    .fetchOne();
            assertThat(actual).isNotNull();
            assertThat(actual.getFeusFeedName()).isEqualTo("Obiwan Kenobi");
        }
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
    void should_list_orphan_feed() {
        List<Entity<WebFeed>> actuals = tested.list(QueryContext.all(Criteria.property(COUNT).eq(0))).collectList().block();
        assertThat(actuals).extracting(Entity::id).containsExactly(
                FeedRecordSamples.FEEDS_RECORDS.getLast().getFeedId());
    }
}