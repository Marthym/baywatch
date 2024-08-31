package fr.ght1pc9kc.baywatch.security.infra.persistence;

import fr.ght1pc9kc.baywatch.security.api.model.UserSettings;
import fr.ght1pc9kc.baywatch.security.infra.mappers.UserSettingsMapper;
import fr.ght1pc9kc.baywatch.tests.samples.infra.UsersSettingsRecordSamples;
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
import reactor.core.scheduler.Schedulers;
import reactor.test.StepVerifier;

import java.util.List;
import java.util.Locale;

import static fr.ght1pc9kc.baywatch.dsl.tables.UsersSettings.USERS_SETTINGS;
import static fr.ght1pc9kc.baywatch.tests.samples.infra.UsersSettingsRecordSamples.DSIDIOUS_SETTINGS;
import static fr.ght1pc9kc.baywatch.tests.samples.infra.UsersSettingsRecordSamples.OKENOBI_SETTINGS;

class UserSettingsRepositoryTest {
    private static final WithInMemoryDatasource wDs = WithInMemoryDatasource.builder().build();
    private static final WithDslContext wDslContext = WithDslContext.builder()
            .setDatasourceExtension(wDs).build();
    private static final WithSampleDataLoaded wSamples = WithSampleDataLoaded.builder(wDslContext)
            .createTablesIfNotExists()
            .addDataset(UsersSettingsRecordSamples.SAMPLE)
            .build();

    @RegisterExtension
    @SuppressWarnings("unused")
    static ChainedExtension chain = ChainedExtension.outer(wDs)
            .append(wDslContext)
            .append(wSamples)
            .register();

    private UserSettingsRepository tested;

    @BeforeEach
    void setUp(DSLContext dslContext) {
        UserSettingsMapper userSettingsMapper = Mappers.getMapper(UserSettingsMapper.class);
        tested = new UserSettingsRepository(dslContext, Schedulers.immediate(), userSettingsMapper);
    }

    @Test
    void should_get_settings(WithSampleDataLoaded.Tracker tracker) {
        tracker.skipNextSampleLoad();
        StepVerifier.create(tested.get(OKENOBI_SETTINGS.getUsseUserId()))
                .assertNext(actual -> Assertions.assertThat(actual.self().preferredLocale())
                        .extracting(Locale::toLanguageTag)
                        .isEqualTo(OKENOBI_SETTINGS.getUssePreferredLocale()))
                .verifyComplete();
    }

    @Test
    void should_create_settings(DSLContext dsl) {
        {
            int count = dsl.fetchCount(USERS_SETTINGS,
                    USERS_SETTINGS.USSE_USER_ID.eq(DSIDIOUS_SETTINGS.getUsseUserId()));
            Assertions.assertThat(count).isZero();
        }
        StepVerifier.create(tested.persist(DSIDIOUS_SETTINGS.getUsseUserId(), new UserSettings(Locale.GERMANY)))
                .assertNext(actual -> Assertions.assertThat(actual.self().preferredLocale())
                        .isEqualTo(Locale.GERMANY))
                .verifyComplete();
        {
            int count = dsl.fetchCount(USERS_SETTINGS,
                    USERS_SETTINGS.USSE_USER_ID.eq(DSIDIOUS_SETTINGS.getUsseUserId()));
            Assertions.assertThat(count).isOne();
        }
    }

    @Test
    void should_update_settings(DSLContext dsl) {
        {
            int count = dsl.fetchCount(USERS_SETTINGS,
                    USERS_SETTINGS.USSE_USER_ID.eq(OKENOBI_SETTINGS.getUsseUserId()));
            Assertions.assertThat(count).isOne();
        }
        StepVerifier.create(tested.persist(OKENOBI_SETTINGS.getUsseUserId(), new UserSettings(Locale.GERMANY)))
                .assertNext(actual -> Assertions.assertThat(actual.self().preferredLocale())
                        .isEqualTo(Locale.GERMANY))
                .verifyComplete();
        {
            List<String> actuals = dsl.select().from(USERS_SETTINGS)
                    .where(USERS_SETTINGS.USSE_USER_ID.eq(OKENOBI_SETTINGS.getUsseUserId()))
                    .fetch(USERS_SETTINGS.USSE_PREFERRED_LOCALE);
            Assertions.assertThat(actuals).hasSize(1);
            Assertions.assertThat(actuals.getFirst()).isEqualTo(Locale.GERMANY.toLanguageTag());
        }
    }
}