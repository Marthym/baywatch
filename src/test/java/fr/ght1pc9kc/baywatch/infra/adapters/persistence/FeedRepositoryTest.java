package fr.ght1pc9kc.baywatch.infra.adapters.persistence;

import fr.ght1pc9kc.baywatch.api.model.Feed;
import fr.ght1pc9kc.baywatch.infra.mappers.RecordToFeedConverter;
import fr.ght1pc9kc.baywatch.infra.samples.FeedRecordSamples;
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

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

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
        tested = new FeedRepository(Schedulers.immediate(), dslContext, defaultConversionService);
    }

    @Test
    void should_list_all_feeds() {
        List<Feed> actuals = tested.list().collectList().block();
        assertThat(actuals).isNotEmpty();
    }

    @Test
    void should_manage_backpressure() {
        List<Feed> actuals = tested.list().limitRate(2).collectList().block();
        assertThat(actuals).hasSize(FeedRecordSamples.SAMPLE.records().size());
    }
}