package fr.ght1pc9kc.baywatch.infra.adapters.persistence;

import fr.ght1pc9kc.baywatch.api.model.Flags;
import fr.ght1pc9kc.baywatch.api.model.News;
import fr.ght1pc9kc.baywatch.api.model.RawNews;
import fr.ght1pc9kc.baywatch.api.model.State;
import fr.ght1pc9kc.baywatch.api.model.request.PageRequest;
import fr.ght1pc9kc.baywatch.api.model.request.filter.Criteria;
import fr.ght1pc9kc.baywatch.api.model.request.pagination.Sort;
import fr.ght1pc9kc.baywatch.dsl.tables.records.NewsFeedsRecord;
import fr.ght1pc9kc.baywatch.dsl.tables.records.NewsRecord;
import fr.ght1pc9kc.baywatch.dsl.tables.records.NewsUserStateRecord;
import fr.ght1pc9kc.baywatch.infra.mappers.NewsFeedsToRecordConverter;
import fr.ght1pc9kc.baywatch.infra.mappers.NewsToRecordConverter;
import fr.ght1pc9kc.baywatch.infra.mappers.RecordToNewsConverter;
import fr.ght1pc9kc.baywatch.infra.mappers.RecordToRawNewsConverter;
import fr.ght1pc9kc.baywatch.infra.samples.FeedRecordSamples;
import fr.ght1pc9kc.baywatch.infra.samples.NewsRecordSamples;
import fr.ght1pc9kc.baywatch.infra.samples.UsersRecordSamples;
import fr.irun.testy.core.extensions.ChainedExtension;
import fr.irun.testy.jooq.WithDatabaseLoaded;
import fr.irun.testy.jooq.WithDslContext;
import fr.irun.testy.jooq.WithInMemoryDatasource;
import fr.irun.testy.jooq.WithSampleDataLoaded;
import org.jooq.DSLContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.core.convert.support.DefaultConversionService;
import reactor.core.scheduler.Schedulers;

import java.net.URI;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;

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
            .build();

    @RegisterExtension
    @SuppressWarnings("unused")
    static ChainedExtension chain = ChainedExtension.outer(wDs)
            .append(wBaywatchDb)
            .append(wDslContext)
            .append(wSamples)
            .register();

    private static final String NEWS_ID = UUID.randomUUID().toString();

    private NewsRepository tested;

    @BeforeEach
    void setUp(DSLContext dslContext) {
        DefaultConversionService conversionService = new DefaultConversionService();
        conversionService.addConverter(new NewsToRecordConverter());
        conversionService.addConverter(new NewsFeedsToRecordConverter());
        conversionService.addConverter(new RecordToRawNewsConverter());
        conversionService.addConverter(new RecordToNewsConverter(conversionService));
        tested = new NewsRepository(Schedulers.immediate(), dslContext, conversionService);
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
                .feedId(FeedRecordSamples.JEDI.getFeedId())
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
        RawNews actual = tested.get("0479255273c08312a67145eec4852293345555eb1145ce0b4243c8314a85ba0c").block();

        assertThat(actual).isEqualTo(RawNews.builder()
                .id("0479255273c08312a67145eec4852293345555eb1145ce0b4243c8314a85ba0c")
                .title("ght1pc9kc.fr 005")
                .link(URI.create("https://blog.ght1pc9kc.fr/005"))
                .publication(Instant.parse("2021-05-10T10:42:42Z"))
                .build());
    }

    @Test
    void should_list_rawnews(WithSampleDataLoaded.Tracker tracker) {
        tracker.skipNextSampleLoad();
        List<RawNews> actuals = tested.list().collectList().block();

        assertThat(actuals).hasSize(50);
    }

    @Test
    void should_list_news(WithSampleDataLoaded.Tracker tracker) {
        tracker.skipNextSampleLoad();
        List<News> actual = tested.userList().collectList().block();
        assertThat(actual).hasSize(50);
        assertThat(actual).extracting(News::getId).startsWith(
                "24abc4ad15dc0ab7824f0192b78cc786a7e57f10c0a50fc0721ac1cc3cd162fc",
                "60b59b7b9b35aa3805af8cf300fcb289055bbc78b012921f231aab5d5921a39c",
                "9034ced51e05837fec112c380b9b9720c81ce79137a000db988ec625cf9e64b3",
                "e35d5a3be1d1fbf1363fbeb1bca2ca248da0dcdfe41b88beb80e9548d9a10c8f",
                "0479255273c08312a67145eec4852293345555eb1145ce0b4243c8314a85ba0c"
        );
    }

    @Test
    void should_list_news_for_user_with_criteria(WithSampleDataLoaded.Tracker tracker) {
        tracker.skipNextSampleLoad();
        PageRequest pageRequest = PageRequest.builder()
                .page(-1).size(-1)
                .sort(Sort.of())
                .filter(Criteria.property("stared").eq(true))
                .build();
        List<News> actual = tested.userList(pageRequest).collectList().block();
        assertThat(actual).allMatch(News::isStared);
    }

    @Test
    void should_get_news_for_user(WithSampleDataLoaded.Tracker tracker) {
        tracker.skipNextSampleLoad();
        NewsRecord expected = NewsRecordSamples.SAMPLE.records().get(5);
        NewsFeedsRecord expectedNefe = NewsRecordSamples.NewsFeedsRecordSample.SAMPLE.records().stream()
                .filter(nf -> nf.getNefeNewsId().equals(expected.getNewsId()))
                .findAny()
                .orElseThrow();
        NewsUserStateRecord expectedState = NewsRecordSamples.NewsUserStateSample.SAMPLE.records().stream()
                .filter(us -> us.getNursNewsId().equals(expected.getNewsId()))
                .findAny().orElseThrow();
        News actual = tested.userGet(expected.getNewsId()).block();

        assertThat(actual).isNotNull();
        assertThat(actual.getLink().toString()).isEqualTo(expected.getNewsLink());
        assertThat(actual.getTitle()).isEqualTo(expected.getNewsTitle());
        assertThat(actual.getId()).isEqualTo(expected.getNewsId());
        assertThat(actual.getFeedId()).isEqualTo(expectedNefe.getNefeFeedId());
        assertThat(actual.isRead()).isEqualTo((expectedState.getNursState() & Flags.READ) != 0);
        assertThat(actual.isStared()).isEqualTo((expectedState.getNursState() & Flags.STAR) != 0);
    }

    @Test
    void should_list_state(WithSampleDataLoaded.Tracker tracker) {
        tracker.skipNextSampleLoad();
        List<Map.Entry<String, State>> actuals = tested.listState(Criteria.property("newsId").in(
                "9034ced51e05837fec112c380b9b9720c81ce79137a000db988ec625cf9e64b3",
                "0479255273c08312a67145eec4852293345555eb1145ce0b4243c8314a85ba0c",
                "24abc4ad15dc0ab7824f0192b78cc786a7e57f10c0a50fc0721ac1cc3cd162fc"
        )).collectList().block();

        assertThat(actuals).containsOnly(
                Map.entry("9034ced51e05837fec112c380b9b9720c81ce79137a000db988ec625cf9e64b3", State.of(2)),
                Map.entry("0479255273c08312a67145eec4852293345555eb1145ce0b4243c8314a85ba0c", State.of(0)),
                Map.entry("24abc4ad15dc0ab7824f0192b78cc786a7e57f10c0a50fc0721ac1cc3cd162fc", State.of(2)));
    }

    @Test
    void should_delete_news(DSLContext dsl) {
        List<String> ids = List.of(
                "24abc4ad15dc0ab7824f0192b78cc786a7e57f10c0a50fc0721ac1cc3cd162fc",
                "e35d5a3be1d1fbf1363fbeb1bca2ca248da0dcdfe41b88beb80e9548d9a10c8f");
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