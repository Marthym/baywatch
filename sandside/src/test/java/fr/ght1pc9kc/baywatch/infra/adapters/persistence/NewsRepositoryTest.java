package fr.ght1pc9kc.baywatch.infra.adapters.persistence;

import fr.ght1pc9kc.baywatch.api.model.Flags;
import fr.ght1pc9kc.baywatch.api.model.News;
import fr.ght1pc9kc.baywatch.api.model.RawNews;
import fr.ght1pc9kc.baywatch.api.model.State;
import fr.ght1pc9kc.baywatch.domain.techwatch.model.QueryContext;
import fr.ght1pc9kc.baywatch.domain.utils.Hasher;
import fr.ght1pc9kc.baywatch.dsl.tables.records.FeedsUsersRecord;
import fr.ght1pc9kc.baywatch.dsl.tables.records.NewsFeedsRecord;
import fr.ght1pc9kc.baywatch.dsl.tables.records.NewsRecord;
import fr.ght1pc9kc.baywatch.dsl.tables.records.NewsUserStateRecord;
import fr.ght1pc9kc.baywatch.infra.mappers.BaywatchMapper;
import fr.ght1pc9kc.baywatch.infra.samples.FeedRecordSamples;
import fr.ght1pc9kc.baywatch.infra.samples.NewsRecordSamples;
import fr.ght1pc9kc.baywatch.infra.samples.UsersRecordSamples;
import fr.ght1pc9kc.juery.api.Criteria;
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
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mapstruct.factory.Mappers;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import reactor.test.StepVerifier;

import java.net.URI;
import java.time.Instant;
import java.util.*;

import static fr.ght1pc9kc.baywatch.dsl.tables.News.NEWS;
import static fr.ght1pc9kc.baywatch.dsl.tables.NewsFeeds.NEWS_FEEDS;
import static fr.ght1pc9kc.baywatch.dsl.tables.NewsUserState.NEWS_USER_STATE;
import static org.assertj.core.api.Assertions.assertThat;

class NewsRepositoryTest {
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
            .addDataset(NewsRecordSamples.NewsFeedsRecordSample.SAMPLE)
            .addDataset(NewsRecordSamples.NewsUserStateSample.SAMPLE)
            .addDataset(FeedRecordSamples.FeedUserRecordSamples.SAMPLE)
            .build();
    private static final String NEWS_ID = UUID.randomUUID().toString();
    @RegisterExtension
    @SuppressWarnings("unused")
    static ChainedExtension chain = ChainedExtension.outer(wDs)
            .append(wBaywatchDb)
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
            assertThat(actual).isEqualTo(0);
            actual = dsl.fetchCount(NEWS_FEEDS, NEWS_FEEDS.NEFE_NEWS_ID.eq(NEWS_ID)
                    .and(NEWS_FEEDS.NEFE_FEED_ID.eq(FeedRecordSamples.JEDI.getFeedId())));
            assertThat(actual).isEqualTo(0);
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
        assertThat(actual).extracting(News::getId).startsWith(
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
        QueryContext qCtx = QueryContext.all(Criteria.property("read").eq(true))
                .withUserId(UsersRecordSamples.LSKYWALKER.getUserId());
        List<News> actual = tested.list(qCtx).collectList().block();
        assertThat(actual).isNotEmpty();
        assertThat(actual).allMatch(News::isRead);
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
        FeedsUsersRecord expectedFeus = FeedRecordSamples.FeedUserRecordSamples.SAMPLE.records().stream()
                .filter(us -> us.getFeusFeedId().equals(expectedNefe.getNefeFeedId()))
                .findAny().orElseThrow();

        QueryContext qCtx = QueryContext.id(expected.getNewsId())
                .withUserId(expectedState.getNursUserId());
        News actual = tested.get(qCtx).block();

        assertThat(actual).isNotNull();
        Assertions.assertAll(
                () -> assertThat(actual.getLink().toString()).isEqualTo(expected.getNewsLink()),
                () -> assertThat(actual.getTitle()).isEqualTo(expected.getNewsTitle()),
                () -> assertThat(actual.getId()).isEqualTo(expected.getNewsId()),
                () -> assertThat(actual.getFeeds()).isEqualTo(Set.of(expectedNefe.getNefeFeedId())),
                () -> assertThat(actual.getTags()).containsOnly(expectedFeus.getFeusTags().split(",")),
                () -> assertThat(actual.isRead()).isEqualTo(State.of(expectedState.getNursState()).isRead()),
                () -> assertThat(actual.isShared()).isEqualTo((expectedState.getNursState() & Flags.SHARED) != 0)
        );
    }

    @Test
    void should_list_state(WithSampleDataLoaded.Tracker tracker) {
        tracker.skipNextSampleLoad();

        String id21 = Hasher.identify(NewsRecordSamples.BASE_TEST_URI.resolve("021"));
        String id22 = Hasher.identify(NewsRecordSamples.BASE_TEST_URI.resolve("022"));
        String id23 = Hasher.identify(NewsRecordSamples.BASE_TEST_URI.resolve("023"));
        List<Map.Entry<String, State>> actuals = tested.listState(Criteria.property("newsId")
                .in(id21, id22, id23))
                .collectList().block();

        assertThat(actuals).containsOnly(
                Map.entry(id21, State.of(0)),
                Map.entry(id22, State.of(1)),
                Map.entry(id23, State.of(2))
        );
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

        tested.addStateFlag(newsId,
                UsersRecordSamples.OKENOBI.getUserId(), removeFlag).block();

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

        tested.removeStateFlag(newsId,
                UsersRecordSamples.LSKYWALKER.getUserId(), removeFlag).block();

        Integer actual = dsl.select(NEWS_USER_STATE.NURS_STATE)
                .from(NEWS_USER_STATE)
                .where(NEWS_USER_STATE.NURS_NEWS_ID.eq(newsId))
                .fetchOne(NEWS_USER_STATE.NURS_STATE);

        assertThat(State.of(actual).isRead()).isEqualTo(expectedRead);
        assertThat(State.of(actual).isShared()).isEqualTo(expectedShared);
    }
}