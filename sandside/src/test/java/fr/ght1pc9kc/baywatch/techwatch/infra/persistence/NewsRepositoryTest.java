package fr.ght1pc9kc.baywatch.techwatch.infra.persistence;

import fr.ght1pc9kc.baywatch.common.api.model.EntitiesProperties;
import fr.ght1pc9kc.baywatch.common.domain.Hasher;
import fr.ght1pc9kc.baywatch.common.infra.mappers.BaywatchMapper;
import fr.ght1pc9kc.baywatch.dsl.tables.records.NewsFeedsRecord;
import fr.ght1pc9kc.baywatch.dsl.tables.records.NewsRecord;
import fr.ght1pc9kc.baywatch.dsl.tables.records.NewsUserStateRecord;
import fr.ght1pc9kc.baywatch.techwatch.api.model.Flags;
import fr.ght1pc9kc.baywatch.techwatch.api.model.News;
import fr.ght1pc9kc.baywatch.techwatch.api.model.RawNews;
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
import org.assertj.core.api.Assertions;
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
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static fr.ght1pc9kc.baywatch.dsl.tables.News.NEWS;
import static fr.ght1pc9kc.baywatch.dsl.tables.NewsFeeds.NEWS_FEEDS;
import static fr.ght1pc9kc.baywatch.dsl.tables.NewsUserState.NEWS_USER_STATE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

class NewsRepositoryTest {
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
    private static final String NEWS_ID = UUID.randomUUID().toString();
    @RegisterExtension
    @SuppressWarnings("unused")
    static ChainedExtension chain = ChainedExtension.outer(wDs)
            .append(wDslContext)
            .append(wSamples)
            .register();
    private NewsRepository tested;

    @BeforeEach
    void setUp(DSLContext dslContext) {
        BaywatchMapper baywatchMapper = Mappers.getMapper(BaywatchMapper.class);
        tested = new NewsRepository(Schedulers.immediate(), dslContext, baywatchMapper);
    }

    @Test
    void should_persist_news(DSLContext dsl) {
        {
            int actual = dsl.fetchCount(NEWS, NEWS.NEWS_ID.eq(NEWS_ID));
            assertThat(actual).isZero();
            actual = dsl.fetchCount(NEWS_FEEDS, NEWS_FEEDS.NEFE_NEWS_ID.eq(NEWS_ID)
                    .and(NEWS_FEEDS.NEFE_FEED_ID.eq(FeedRecordSamples.JEDI.getFeedId())));
            assertThat(actual).isZero();
        }

        News news = News.builder().raw(
                        RawNews.builder()
                                .id(NEWS_ID)
                                .title("Obiwan Kenobi")
                                .link(URI.create("http://obiwan.kenobi.jedi/"))
                                .publication(Instant.now())
                                .build())
                .feeds(Set.of(FeedRecordSamples.JEDI.getFeedId()))
                .state(State.NONE)
                .build();

        tested.persist(Collections.singleton(news)).block();

        {
            dsl.selectFrom(NEWS).execute();
            int actual = dsl.fetchCount(NEWS, NEWS.NEWS_ID.eq(NEWS_ID));
            assertThat(actual).isEqualTo(1);
            actual = dsl.fetchCount(NEWS_FEEDS, NEWS_FEEDS.NEFE_NEWS_ID.eq(NEWS_ID)
                    .and(NEWS_FEEDS.NEFE_FEED_ID.eq(FeedRecordSamples.JEDI.getFeedId())));
            assertThat(actual).isEqualTo(1);
        }
    }

    @Test
    void should_get_raw_news(WithSampleDataLoaded.Tracker tracker) {
        tracker.skipNextSampleLoad();
        Mono<RawNews> actual = tested.get(QueryContext.id("8a1161a7d2fc70fd5e865d3394eddfc0dbad40a792973f9dad50ff62afdb088b"))
                .map(News::getRaw);

        RawNews expected = RawNews.builder()
                .id("8a1161a7d2fc70fd5e865d3394eddfc0dbad40a792973f9dad50ff62afdb088b")
                .title("blog.ght1pc9kc.fr 005")
                .link(URI.create("https://blog.ght1pc9kc.fr/005"))
                .publication(Instant.parse("2021-05-10T10:42:42Z"))
                .build();

        StepVerifier.create(actual)
                .expectNext(expected)
                .verifyComplete();
    }

    @Test
    void should_list_rawnews(WithSampleDataLoaded.Tracker tracker) {
        tracker.skipNextSampleLoad();
        List<News> actuals = tested.list().collectList().block();

        assertThat(actuals).hasSize(51);
    }

    @Test
    void should_list_news(WithSampleDataLoaded.Tracker tracker) {
        tracker.skipNextSampleLoad();
        List<News> actual = tested.list().collectList().block();
        assertThat(actual).hasSize(51);
        assertThat(actual).extracting(News::id).startsWith(
                "134d1ba72a9cf41060f39349c8042d203c02b69b9082383153c423f3633a419f",
                "155759bd7796715f439c9c25739bc34b8fa4cac7036f8bdd61148a4ceac76a57",
                "1fff2b3142d5d27677567a0da6811c09a428e7636f169d77006dede02ee6cec0",
                "22f530b91e1dac4854cd3273b1ca45784e08a00fac25ca01792c6989db152fc0",
                "26897ef9efde81583efc3b5e690d00c548a8e6fd8d9d8de08fc6543da5fffd9d"
        );
    }

    @Test
    void should_list_news_for_user_with_criteria(WithSampleDataLoaded.Tracker tracker) {
        tracker.skipNextSampleLoad();
        QueryContext qCtx = QueryContext.all(Criteria.property(EntitiesProperties.NEWS_ID).in(
                "134d1ba72a9cf41060f39349c8042d203c02b69b9082383153c423f3633a419f",
                "155759bd7796715f439c9c25739bc34b8fa4cac7036f8bdd61148a4ceac76a57",
                "1fff2b3142d5d27677567a0da6811c09a428e7636f169d77006dede02ee6cec0",
                "22f530b91e1dac4854cd3273b1ca45784e08a00fac25ca01792c6989db152fc0",
                "26897ef9efde81583efc3b5e690d00c548a8e6fd8d9d8de08fc6543da5fffd9d"
        )).withUserId(UsersRecordSamples.LSKYWALKER.getUserId());

        StepVerifier.create(tested.list(qCtx))
                .assertNext(actual -> Assertions.assertThat(actual.id())
                        .isEqualTo("134d1ba72a9cf41060f39349c8042d203c02b69b9082383153c423f3633a419f"))
                .assertNext(actual -> Assertions.assertThat(actual.id())
                        .isEqualTo("155759bd7796715f439c9c25739bc34b8fa4cac7036f8bdd61148a4ceac76a57"))
                .assertNext(actual -> Assertions.assertThat(actual.id())
                        .isEqualTo("1fff2b3142d5d27677567a0da6811c09a428e7636f169d77006dede02ee6cec0"))
                .assertNext(actual -> Assertions.assertThat(actual.id())
                        .isEqualTo("22f530b91e1dac4854cd3273b1ca45784e08a00fac25ca01792c6989db152fc0"))
                .assertNext(actual -> Assertions.assertThat(actual.id())
                        .isEqualTo("26897ef9efde81583efc3b5e690d00c548a8e6fd8d9d8de08fc6543da5fffd9d"))
                .verifyComplete();
    }

    @Test
    void should_get_news_for_user(WithSampleDataLoaded.Tracker tracker) {
        tracker.skipNextSampleLoad();
        NewsRecord expected = NewsRecordSamples.SAMPLE.records().get(4);
        NewsFeedsRecord expectedNefe = NewsRecordSamples.NewsFeedsRecordSample.SAMPLE.records().stream()
                .filter(nf -> nf.getNefeNewsId().equals(expected.getNewsId()))
                .findAny()
                .orElseThrow();
        NewsUserStateRecord expectedState = NewsRecordSamples.NewsUserStateSample.SAMPLE.records().stream()
                .filter(us -> us.getNursNewsId().equals(expected.getNewsId()))
                .findAny().orElseThrow();
        FeedRecordSamples.FeedUserRecordSamples.SAMPLE.records().stream()
                .filter(us -> us.getFeusFeedId().equals(expectedNefe.getNefeFeedId()))
                .findAny().orElseThrow();

        QueryContext qCtx = QueryContext.id(expected.getNewsId())
                .withUserId(expectedState.getNursUserId());

        StepVerifier.create(tested.get(qCtx))
                .assertNext(actual -> {
                    assertThat(actual).isNotNull();
                    assertAll(
                            () -> assertThat(actual.link()).hasToString(expected.getNewsLink()),
                            () -> assertThat(actual.title()).isEqualTo(expected.getNewsTitle()),
                            () -> assertThat(actual.id()).isEqualTo(expected.getNewsId()),
                            () -> assertThat(actual.getFeeds()).isEqualTo(Set.of(expectedNefe.getNefeFeedId())),
                            () -> assertThat(actual.isRead()).isEqualTo(State.of(expectedState.getNursState()).isRead()),
                            () -> assertThat(actual.isShared()).isEqualTo((expectedState.getNursState() & Flags.SHARED) != 0)
                    );
                }).verifyComplete();
    }


    @Test
    void should_delete_news(DSLContext dsl) {
        List<String> ids = List.of(
                Hasher.identify(NewsRecordSamples.BASE_TEST_URI.resolve("024")),
                Hasher.identify(NewsRecordSamples.BASE_TEST_URI.resolve("042"))
        );
        {
            int countNews = dsl.fetchCount(NEWS, NEWS.NEWS_ID.in(ids));
            assertThat(countNews).isEqualTo(ids.size());
            int countFeed = dsl.fetchCount(NEWS_FEEDS, NEWS_FEEDS.NEFE_NEWS_ID.in(ids));
            assertThat(countFeed).isEqualTo(ids.size());
            int countState = dsl.fetchCount(NEWS_USER_STATE, NEWS_USER_STATE.NURS_NEWS_ID.in(ids));
            assertThat(countState).isEqualTo(ids.size());
        }
        tested.delete(ids).block();

        {
            int countNews = dsl.fetchCount(NEWS, NEWS.NEWS_ID.in(ids));
            assertThat(countNews).isZero();
            int countFeed = dsl.fetchCount(NEWS_FEEDS, NEWS_FEEDS.NEFE_NEWS_ID.in(ids));
            assertThat(countFeed).isZero();
            int countState = dsl.fetchCount(NEWS_USER_STATE, NEWS_USER_STATE.NURS_NEWS_ID.in(ids));
            assertThat(countState).isZero();
        }
    }

}