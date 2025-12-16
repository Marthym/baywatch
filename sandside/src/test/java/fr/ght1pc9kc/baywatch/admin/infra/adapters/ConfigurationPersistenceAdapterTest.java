package fr.ght1pc9kc.baywatch.admin.infra.adapters;

import fr.ght1pc9kc.baywatch.admin.domain.ports.ConfigurationPersistencePort;
import fr.ght1pc9kc.baywatch.admin.infra.samples.ConfigurationRecordSamples;
import fr.ght1pc9kc.baywatch.dsl.tables.records.ConfigurationRecord;
import fr.ght1pc9kc.entity.api.Entity;
import fr.ght1pc9kc.juery.api.Criteria;
import fr.ght1pc9kc.juery.api.PageRequest;
import fr.ght1pc9kc.juery.api.Pagination;
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
import reactor.core.scheduler.Schedulers;
import reactor.test.StepVerifier;

import java.util.List;
import java.util.Map;

import static fr.ght1pc9kc.baywatch.dsl.tables.Configuration.CONFIGURATION;

class ConfigurationPersistenceAdapterTest {
    private static final WithInMemoryDatasource wDs = WithInMemoryDatasource.builder().build();
    private static final WithDslContext wDslContext = WithDslContext.builder()
            .setDatasourceExtension(wDs).build();
    private static final WithSampleDataLoaded wSamples = WithSampleDataLoaded.builder(wDslContext)
            .createTablesIfNotExists()
            .addDataset(ConfigurationRecordSamples.SAMPLE)
            .build();

    @RegisterExtension
    @SuppressWarnings("unused")
    static ChainedExtension chain = ChainedExtension.outer(wDs)
            .append(wDslContext)
            .append(wSamples)
            .register();

    private ConfigurationPersistencePort tested;

    @BeforeEach
    void setUp(DSLContext dsl) {
        tested = new ConfigurationPersistenceAdapter(dsl, Schedulers.immediate());
    }

    @Test
    void should_list_all_configurations(WithSampleDataLoaded.Tracker tracker) {
        tracker.skipNextSampleLoad();

        StepVerifier.create(tested.list(PageRequest.all()))
                .assertNext(config -> SoftAssertions.assertSoftly(softly -> {
                    softly.assertThat(config.id()).isEqualTo("CF01K2F88DYNCAPEAKCZEWXY3Z7N");
                    softly.assertThat(config.self().getKey()).isEqualTo("jedi.master");
                    softly.assertThat(config.self().getValue()).isEqualTo("Yoda");
                }))
                .expectNextCount(6)
                .verifyComplete();
    }

    @Test
    void should_filter_configurations_by_name_startswith(WithSampleDataLoaded.Tracker tracker) {
        tracker.skipNextSampleLoad();

        PageRequest request = PageRequest.all(Criteria.property("name").startWith("jedi"));

        StepVerifier.create(tested.list(request))
                .assertNext(config -> SoftAssertions.assertSoftly(softly -> {
                    softly.assertThat(config.id()).isEqualTo("CF01K2F88DYNCAPEAKCZEWXY3Z7N");
                    softly.assertThat(config.self().getKey()).isEqualTo("jedi.master");
                    softly.assertThat(config.self().getValue()).isEqualTo("Yoda");
                }))
                .assertNext(config -> SoftAssertions.assertSoftly(softly -> {
                    softly.assertThat(config.id()).isEqualTo("CF01K2F88EB6VJMH79EGGTTED040");
                    softly.assertThat(config.self().getKey()).isEqualTo("jedi.knight");
                    softly.assertThat(config.self().getValue()).isEqualTo("Luke Skywalker");
                }))
                .verifyComplete();
    }

    @Test
    void should_filter_configurations_by_name_equals(WithSampleDataLoaded.Tracker tracker) {
        tracker.skipNextSampleLoad();

        PageRequest request = PageRequest.all(Criteria.property("name").eq("sith.lord"));

        StepVerifier.create(tested.list(request))
                .assertNext(config -> SoftAssertions.assertSoftly(softly -> {
                    softly.assertThat(config.id()).isEqualTo("CF01K2F88ERJ2FZ7EQSXVM8XVWT0");
                    softly.assertThat(config.self().getKey()).isEqualTo("sith.lord");
                    softly.assertThat(config.self().getValue()).isEqualTo("Darth Vader");
                }))
                .verifyComplete();
    }

    @Test
    void should_filter_configurations_by_id(WithSampleDataLoaded.Tracker tracker) {
        tracker.skipNextSampleLoad();

        PageRequest request = PageRequest.all(Criteria.property("id").eq("CF01K2F88G9XYWS9PXC4Y4X0EQAR"));

        StepVerifier.create(tested.list(request))
                .assertNext(config -> SoftAssertions.assertSoftly(softly -> {
                    softly.assertThat(config.id()).isEqualTo("CF01K2F88G9XYWS9PXC4Y4X0EQAR");
                    softly.assertThat(config.self().getKey()).isEqualTo("force.power.level");
                    softly.assertThat(config.self().getValue()).isEqualTo("9001");
                }))
                .verifyComplete();
    }

    @Test
    void should_return_empty_when_no_configuration_matches_filter(WithSampleDataLoaded.Tracker tracker) {
        tracker.skipNextSampleLoad();

        PageRequest request = PageRequest.all(Criteria.property("name").startWith("ewok"));

        StepVerifier.create(tested.list(request))
                .verifyComplete();
    }

    @Test
    void should_limit_results_with_pagination(WithSampleDataLoaded.Tracker tracker) {
        tracker.skipNextSampleLoad();

        PageRequest request = PageRequest.of(Pagination.of(0, 2), Criteria.none());

        StepVerifier.create(tested.list(request))
                .expectNextCount(2)
                .verifyComplete();
    }

    @Test
    void should_handle_empty_database(DSLContext dsl) {
        dsl.deleteFrom(CONFIGURATION).execute();

        StepVerifier.create(tested.list(PageRequest.all()))
                .verifyComplete();
    }

    @Test
    void should_verify_database_contains_sample_data(DSLContext dsl) {
        int count = dsl.fetchCount(CONFIGURATION);
        Assertions.assertThat(count).isEqualTo(ConfigurationRecordSamples.SAMPLE.records().size());
    }

    @Test
    void should_persist_new_configurations_and_generate_ids(DSLContext dsl) {
        int beforeCount = dsl.fetchCount(CONFIGURATION);

        var params = List.of(
                Map.entry("mail.smtp.host", "smtp.jedi.temple"),
                Map.entry("mail.smtp.port", "587")
        );

        StepVerifier.create(tested.persist(params).collectList())
                .assertNext(saved -> SoftAssertions.assertSoftly(softly -> {
                    softly.assertThat(saved).hasSize(2);

                    Entity<Map.Entry<String, String>> first = saved.getFirst();
                    softly.assertThat(first.id()).startsWith("CF");
                    softly.assertThat(first.self().getKey()).isEqualTo("mail.smtp.host");
                    softly.assertThat(first.self().getValue()).isEqualTo("smtp.jedi.temple");

                    Entity<Map.Entry<String, String>> second = saved.get(1);
                    softly.assertThat(second.id()).startsWith("CF");
                    softly.assertThat(second.self().getKey()).isEqualTo("mail.smtp.port");
                    softly.assertThat(second.self().getValue()).isEqualTo("587");
                }))
                .verifyComplete();

        int afterCount = dsl.fetchCount(CONFIGURATION);
        Assertions.assertThat(afterCount).isEqualTo(beforeCount + 2);

        ConfigurationRecord host = dsl.selectFrom(CONFIGURATION)
                .where(CONFIGURATION.CONF_NAME.eq("mail.smtp.host"))
                .fetchOne();
        Assertions.assertThat(host).isNotNull();
        Assertions.assertThat(host.getConfValue()).isEqualTo("smtp.jedi.temple");
    }

    @Test
    void should_update_existing_configuration_without_creating_new_row(DSLContext dsl) {
        // On prend une entrée existante du dataset
        String key = "jedi.master";

        ConfigurationRecord before = dsl.selectFrom(CONFIGURATION)
                .where(CONFIGURATION.CONF_NAME.eq(key))
                .fetchOne();
        Assertions.assertThat(before).isNotNull();

        int beforeCount = dsl.fetchCount(CONFIGURATION);
        String existingId = before.getConfId();

        StepVerifier.create(tested.persist(List.of(Map.entry(key, "Mace Windu"))).single())
                .assertNext(saved -> SoftAssertions.assertSoftly(softly -> {
                    softly.assertThat(saved.id()).isEqualTo(existingId);
                    softly.assertThat(saved.self().getKey()).isEqualTo(key);
                    softly.assertThat(saved.self().getValue()).isEqualTo("Mace Windu");
                }))
                .verifyComplete();

        int afterCount = dsl.fetchCount(CONFIGURATION);
        Assertions.assertThat(afterCount).isEqualTo(beforeCount);

        ConfigurationRecord after = dsl.selectFrom(CONFIGURATION)
                .where(CONFIGURATION.CONF_NAME.eq(key))
                .fetchOne();
        Assertions.assertThat(after).isNotNull();
        Assertions.assertThat(after.getConfId()).isEqualTo(existingId);
        Assertions.assertThat(after.getConfValue()).isEqualTo("Mace Windu");
    }

    @Test
    void should_noop_when_persisting_empty_collection(DSLContext dsl) {
        int beforeCount = dsl.fetchCount(CONFIGURATION);

        StepVerifier.create(tested.persist(List.of()))
                .verifyComplete();

        int afterCount = dsl.fetchCount(CONFIGURATION);
        Assertions.assertThat(afterCount).isEqualTo(beforeCount);
    }
}