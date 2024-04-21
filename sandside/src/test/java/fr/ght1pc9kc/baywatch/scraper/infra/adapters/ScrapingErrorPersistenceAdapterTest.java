package fr.ght1pc9kc.baywatch.scraper.infra.adapters;

import fr.ght1pc9kc.baywatch.common.domain.QueryContext;
import fr.ght1pc9kc.baywatch.dsl.tables.FeedsErrors;
import fr.ght1pc9kc.baywatch.scraper.api.model.ScrapingError;
import fr.ght1pc9kc.baywatch.scraper.infra.config.ScraperMapper;
import fr.ght1pc9kc.baywatch.tests.samples.FeedSamples;
import fr.ght1pc9kc.baywatch.tests.samples.infra.FeedsErrorsRecordSamples;
import fr.ght1pc9kc.entity.api.Entity;
import fr.ght1pc9kc.juery.api.Criteria;
import fr.ght1pc9kc.testy.core.extensions.ChainedExtension;
import fr.ght1pc9kc.testy.jooq.WithDslContext;
import fr.ght1pc9kc.testy.jooq.WithInMemoryDatasource;
import fr.ght1pc9kc.testy.jooq.WithSampleDataLoaded;
import org.assertj.core.api.Assertions;
import org.assertj.core.api.SoftAssertions;
import org.jooq.DSLContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.mapstruct.factory.Mappers;
import reactor.core.scheduler.Schedulers;
import reactor.test.StepVerifier;

import java.time.Instant;
import java.util.List;

class ScrapingErrorPersistenceAdapterTest {
    private static final WithInMemoryDatasource wDs = WithInMemoryDatasource.builder().build();
    private static final WithDslContext wDslContext = WithDslContext.builder()
            .setDatasourceExtension(wDs).build();
    private static final WithSampleDataLoaded wSamples = WithSampleDataLoaded.builder(wDslContext)
            .createTablesIfNotExists()
            .addDataset(FeedsErrorsRecordSamples.SAMPLE)
            .build();

    @RegisterExtension
    @SuppressWarnings("unused")
    static ChainedExtension chain = ChainedExtension.outer(wDs)
            .append(wDslContext)
            .append(wSamples)
            .register();

    private ScrapingErrorPersistenceAdapter tested;

    @BeforeEach
    void setUp(DSLContext dsl) {
        ScraperMapper mapper = Mappers.getMapper(ScraperMapper.class);
        tested = new ScrapingErrorPersistenceAdapter(dsl, mapper, Schedulers.immediate());
    }

    @Test
    void should_persist_feed_scraping_errors(DSLContext dsl) {
        Instant now = Instant.parse("2024-04-01T15:10:42Z");
        List<Entity<ScrapingError>> toPersist = List.of(
                Entity.identify(new ScrapingError(now, now, 403, "Forbidden")).withId(FeedSamples.JEDI.id()),
                Entity.identify(new ScrapingError(now, now, 403, "Forbidden")).withId(FeedSamples.UNSECURE_PROTOCOL.id())
        );

        int count = dsl.fetchCount(FeedsErrors.FEEDS_ERRORS);
        Assertions.assertThat(count).isEqualTo(FeedsErrorsRecordSamples.SAMPLE.records().size());

        StepVerifier.create(tested.persist(toPersist))
                .assertNext(jedi -> SoftAssertions.assertSoftly(softly -> {
                    softly.assertThat(jedi.id()).isEqualTo(FeedSamples.JEDI.id());
                    softly.assertThat(jedi.self().status()).isEqualTo(403);
                    softly.assertThat(jedi.self().since()).isEqualTo(Instant.parse("2024-03-30T12:42:24Z"));
                    softly.assertThat(jedi.self().lastTime()).isEqualTo(now);
                }))
                .assertNext(second -> SoftAssertions.assertSoftly(softly -> {
                    softly.assertThat(second.id()).isEqualTo(FeedSamples.UNSECURE_PROTOCOL.id());
                    softly.assertThat(second.self().status()).isEqualTo(403);
                    softly.assertThat(second.self().since()).isEqualTo(now);
                    softly.assertThat(second.self().lastTime()).isEqualTo(now);
                }))
                .verifyComplete();

        count = dsl.fetchCount(FeedsErrors.FEEDS_ERRORS);
        Assertions.assertThat(count).isEqualTo(FeedsErrorsRecordSamples.SAMPLE.records().size() + 1);
    }

    @Test
    void should_list_scraping_errors(WithSampleDataLoaded.Tracker tracker) {
        tracker.skipNextSampleLoad();
        Instant since = Instant.parse("2024-03-30T12:42:24Z");
        Instant last = Instant.parse("2024-03-30T13:12:24Z");

        StepVerifier.create(tested.list(QueryContext.all(Criteria.none())))
                .assertNext(jedi -> SoftAssertions.assertSoftly(softly -> {
                    softly.assertThat(jedi.id()).isEqualTo(FeedSamples.JEDI.id());
                    softly.assertThat(jedi.self().status()).isEqualTo(404);
                    softly.assertThat(jedi.self().since()).isEqualTo(since);
                    softly.assertThat(jedi.self().lastTime()).isEqualTo(last);
                }))
                .assertNext(second -> SoftAssertions.assertSoftly(softly -> {
                    softly.assertThat(second.id()).isEqualTo(FeedSamples.SITH.id());
                    softly.assertThat(second.self().status()).isEqualTo(404);
                    softly.assertThat(second.self().since()).isEqualTo(since);
                    softly.assertThat(second.self().lastTime()).isEqualTo(last);
                }))
                .verifyComplete();
    }

    @Test
    void should_delete_errors(DSLContext dsl) {
        int count = dsl.fetchCount(FeedsErrors.FEEDS_ERRORS);
        Assertions.assertThat(count).isEqualTo(FeedsErrorsRecordSamples.SAMPLE.records().size());

        StepVerifier.create(tested.delete(QueryContext.id(FeedSamples.JEDI.id())))
                .verifyComplete();

        count = dsl.fetchCount(FeedsErrors.FEEDS_ERRORS);
        Assertions.assertThat(count).isEqualTo(FeedsErrorsRecordSamples.SAMPLE.records().size() - 1);
    }
}