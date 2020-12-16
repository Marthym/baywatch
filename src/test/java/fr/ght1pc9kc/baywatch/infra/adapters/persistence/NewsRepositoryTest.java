package fr.ght1pc9kc.baywatch.infra.adapters.persistence;

import fr.ght1pc9kc.baywatch.api.model.News;
import fr.ght1pc9kc.baywatch.api.model.RawNews;
import fr.ght1pc9kc.baywatch.api.model.search.Criteria;
import fr.ght1pc9kc.baywatch.infra.mappers.NewsFeedsToRecordConverter;
import fr.ght1pc9kc.baywatch.infra.mappers.NewsToRecordConverter;
import fr.ght1pc9kc.baywatch.infra.mappers.RecordToNewsConverter;
import fr.ght1pc9kc.baywatch.infra.samples.FeedRecordSamples;
import fr.ght1pc9kc.baywatch.infra.samples.NewsRecordSamples;
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
import java.util.UUID;

import static fr.ght1pc9kc.baywatch.dsl.tables.News.NEWS;
import static fr.ght1pc9kc.baywatch.dsl.tables.NewsFeeds.NEWS_FEEDS;
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
            .addDataset(FeedRecordSamples.SAMPLE)
            .addDataset(NewsRecordSamples.SAMPLE)
            .addDataset(NewsRecordSamples.NewsFeedsRecordSample.SAMPLE)
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
        DefaultConversionService defaultConversionService = new DefaultConversionService();
        defaultConversionService.addConverter(new NewsToRecordConverter());
        defaultConversionService.addConverter(new RecordToNewsConverter());
        defaultConversionService.addConverter(new NewsFeedsToRecordConverter());
        tested = new NewsRepository(Schedulers.immediate(), dslContext, defaultConversionService);
    }

    @Test
    void should_persist_news(DSLContext dsl) {
        {
            int actual = dsl.fetchCount(NEWS, NEWS.NEWS_ID.eq(NEWS_ID));
            assertThat(actual).isEqualTo(0);
            actual = dsl.fetchCount(NEWS_FEEDS, NEWS_FEEDS.NEFE_NEWS_ID.eq(NEWS_ID)
                    .and(NEWS_FEEDS.NEFE_FEED_ID.eq(42)));
            assertThat(actual).isEqualTo(0);
        }

        News news = News.builder().raw(
                RawNews.builder()
                        .id(NEWS_ID)
                        .title("Obiwan Kenobi")
                        .link(URI.create("http://obiwan.kenobi.jedi/"))
                        .publication(Instant.now())
                        .build())
                .feedId(42)
                .build();

        tested.persist(Collections.singleton(news)).block();

        {
            dsl.selectFrom(NEWS).execute();
            int actual = dsl.fetchCount(NEWS, NEWS.NEWS_ID.eq(NEWS_ID));
            assertThat(actual).isEqualTo(1);
            actual = dsl.fetchCount(NEWS_FEEDS, NEWS_FEEDS.NEFE_NEWS_ID.eq(NEWS_ID)
                    .and(NEWS_FEEDS.NEFE_FEED_ID.eq(42)));
            assertThat(actual).isEqualTo(1);
        }
    }

    @Test
    void should_list_news() {
        List<News> actual = tested.list().collectList().block();
        assertThat(actual).hasSize(50);
        assertThat(actual).extracting(News::getId).startsWith(
                "24abc4ad15dc0ab7824f0192b78cc786a7e57f10c0a50fc0721ac1cc3cd162fc",
                "60b59b7b9b35aa3805af8cf300fcb289055bbc78b012921f231aab5d5921a39c",
                "9034ced51e05837fec112c380b9b9720c81ce79137a000db988ec625cf9e64b3",
                "e35d5a3be1d1fbf1363fbeb1bca2ca248da0dcdfe41b88beb80e9548d9a10c8f",
                "0479255273c08312a67145eec4852293345555eb1145ce0b4243c8314a85ba0c"
        );
    }

//    @Test
//    void should_list_news_with_filters() {
//        List<News> actual = tested.list(Criteria.property("stared").eq(true)).collectList().block();
//        assertThat(actual).allMatch(News::isStared);
//    }
}