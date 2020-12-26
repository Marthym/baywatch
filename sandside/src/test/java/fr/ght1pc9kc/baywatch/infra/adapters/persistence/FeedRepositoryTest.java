package fr.ght1pc9kc.baywatch.infra.adapters.persistence;

import fr.ght1pc9kc.baywatch.api.AuthenticationFacade;
import fr.ght1pc9kc.baywatch.api.model.Feed;
import fr.ght1pc9kc.baywatch.api.model.RawFeed;
import fr.ght1pc9kc.baywatch.api.model.User;
import fr.ght1pc9kc.baywatch.domain.utils.Hasher;
import fr.ght1pc9kc.baywatch.dsl.tables.records.FeedsRecord;
import fr.ght1pc9kc.baywatch.dsl.tables.records.FeedsUsersRecord;
import fr.ght1pc9kc.baywatch.infra.mappers.*;
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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.core.convert.support.DefaultConversionService;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.net.URI;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import static fr.ght1pc9kc.baywatch.dsl.tables.Feeds.FEEDS;
import static fr.ght1pc9kc.baywatch.dsl.tables.FeedsUsers.FEEDS_USERS;
import static fr.ght1pc9kc.baywatch.dsl.tables.NewsFeeds.NEWS_FEEDS;
import static fr.ght1pc9kc.baywatch.infra.samples.UsersRecordSamples.OKENOBI;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class FeedRepositoryTest {
    private static final WithInMemoryDatasource wDs = WithInMemoryDatasource.builder().build();
    private static final WithDatabaseLoaded wBaywatchDb = WithDatabaseLoaded.builder()
            .setDatasourceExtension(wDs)
            .useFlywayDefaultLocation()
            .build();
    private static final WithDslContext wDslContext = WithDslContext.builder()
            .setDatasourceExtension(wDs).build();
    private static final WithSampleDataLoaded wSamples = WithSampleDataLoaded.builder(wDslContext)
            .addDataset(FeedRecordSamples.SAMPLE)
            .addDataset(UsersRecordSamples.SAMPLE)
            .addDataset(NewsRecordSamples.SAMPLE)
            .addDataset(NewsRecordSamples.NewsFeedsRecordSample.SAMPLE)
            .addDataset(FeedsUsersRecordSample.SAMPLE)
            .build();

    @RegisterExtension
    @SuppressWarnings("unused")
    static ChainedExtension chain = ChainedExtension.outer(wDs)
            .append(wBaywatchDb)
            .append(wDslContext)
            .append(wSamples)
            .register();

    private FeedRepository tested;

    @BeforeEach
    void setUp(DSLContext dslContext) {
        DefaultConversionService defaultConversionService = new DefaultConversionService();
        defaultConversionService.addConverter(new RecordToFeedConverter());
        defaultConversionService.addConverter(new RecordToRawFeedConverter());
        defaultConversionService.addConverter(new FeedsUsersToRecordConverter());
        defaultConversionService.addConverter(new FeedToRecordConverter());
        AuthenticationFacade authFacade = mock(AuthenticationFacade.class);
        when(authFacade.getConnectedUser()).thenReturn(Mono.just(
                User.builder()
                        .id(OKENOBI.getUserId())
                        .login(OKENOBI.getUserLogin())
                        .name(OKENOBI.getUserName())
                        .mail(OKENOBI.getUserEmail())
                        .build()));
        tested = new FeedRepository(Schedulers.immediate(), dslContext, defaultConversionService, authFacade);
    }

    @Test
    void should_get_user_feed() {
        FeedsRecord expected = FeedRecordSamples.SAMPLE.records().get(0);
        Feed actual = tested.get(expected.getFeedId()).block();

        assertThat(actual).isEqualTo(Feed.builder()
                .raw(RawFeed.builder()
                        .id(expected.getFeedId())
                        .url(URI.create(expected.getFeedUrl()))
                        .name(expected.getFeedName())
                        .lastWatch(DateUtils.toInstant(expected.getFeedLastWatch()))
                        .build())
                .tags(Set.of("java", "spring"))
                .build());
    }

    @Test
    void should_list_all_feeds(WithSampleDataLoaded.Tracker dbTracker) {
        dbTracker.skipNextSampleLoad();
        List<Feed> actuals = tested.list().collectList().block();
        assertThat(actuals).isNotEmpty();
    }

    @Test
    void should_manage_backpressure(WithSampleDataLoaded.Tracker dbTracker) {
        dbTracker.skipNextSampleLoad();
        List<Feed> actuals = tested.list().limitRate(2).collectList().block();
        assertThat(actuals).hasSize(FeedRecordSamples.SAMPLE.records().size());
    }

    @Test
    void should_persist_feeds(DSLContext dsl) {
        Feed expected = Feed.builder()
                .raw(RawFeed.builder()
                        .id(Hasher.sha3("https://obiwan.kenobi.jedi/.rss"))
                        .url(URI.create("https://obiwan.kenobi.jedi/.rss"))
                        .name("Obiwan Kenobi")
                        .build())
                .tags(Set.of("jedi", "light"))
                .build();

        tested.persist(Collections.singleton(expected)).block();

        {
            FeedsRecord actual = dsl.selectFrom(FEEDS).where(FEEDS.FEED_ID.eq(expected.getId())).fetchOne();
            assertThat(actual).isNotNull();
            assertThat(actual.getFeedName()).isEqualTo(expected.getName());
        }
        {
            FeedsUsersRecord actual = dsl.selectFrom(FEEDS_USERS).where(FEEDS_USERS.FEUS_FEED_ID.eq(expected.getId()))
                    .fetchOne();
            assertThat(actual).isNotNull();
            assertThat(actual.getFeusUserId()).isEqualTo(OKENOBI.getUserId());
            assertThat(actual.getFeusTags()).isEqualTo(String.join(",", expected.getTags()));
        }
    }

    @Test
    void should_delete_feeds(DSLContext dsl) {
        List<String> ids = List.of(
                "530a28c0c7a93eb97c46114bdd6b276b665b3f3100548e1a849ece1955e45b57",
                "75ac07c9803c0dffe3d1769a3ec0037068bc040cff01032721dbb1d79f68e95a");
        {
            int countNews = dsl.fetchCount(FEEDS, FEEDS.FEED_ID.in(ids));
            assertThat(countNews).isEqualTo(ids.size());
            int countFeed = dsl.fetchCount(NEWS_FEEDS, NEWS_FEEDS.NEFE_FEED_ID.in(ids));
            assertThat(countFeed).isEqualTo(20);
            int countUser = dsl.fetchCount(FEEDS_USERS, FEEDS_USERS.FEUS_FEED_ID.in(ids));
            assertThat(countUser).isEqualTo(ids.size());
        }

        tested.delete(ids).block();

        {
            int countNews = dsl.fetchCount(FEEDS, FEEDS.FEED_ID.in(ids));
            assertThat(countNews).isEqualTo(0);
            int countFeed = dsl.fetchCount(NEWS_FEEDS, NEWS_FEEDS.NEFE_FEED_ID.in(ids));
            assertThat(countFeed).isEqualTo(0);
            int countState = dsl.fetchCount(FEEDS_USERS, FEEDS_USERS.FEUS_FEED_ID.in(ids));
            assertThat(countState).isEqualTo(0);
        }
    }
}