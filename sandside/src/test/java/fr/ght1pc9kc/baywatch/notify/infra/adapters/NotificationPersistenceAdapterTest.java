package fr.ght1pc9kc.baywatch.notify.infra.adapters;

import com.fasterxml.jackson.databind.ObjectMapper;
import fr.ght1pc9kc.baywatch.dsl.tables.Notifications;
import fr.ght1pc9kc.baywatch.notify.api.model.EventType;
import fr.ght1pc9kc.baywatch.notify.api.model.ServerEvent;
import fr.ght1pc9kc.baywatch.notify.domain.ports.NotificationPersistencePort;
import fr.ght1pc9kc.baywatch.notify.infra.samples.NotificationsRecordSamples;
import fr.ght1pc9kc.baywatch.tests.samples.UserSamples;
import fr.ght1pc9kc.testy.core.extensions.ChainedExtension;
import fr.ght1pc9kc.testy.jooq.WithDslContext;
import fr.ght1pc9kc.testy.jooq.WithInMemoryDatasource;
import fr.ght1pc9kc.testy.jooq.WithSampleDataLoaded;
import org.jooq.DSLContext;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;
import reactor.test.StepVerifier;

import static org.assertj.core.api.Assertions.assertThat;

class NotificationPersistenceAdapterTest {
    private static final WithInMemoryDatasource wDs = WithInMemoryDatasource.builder().build();
    private static final WithDslContext wDslContext = WithDslContext.builder()
            .setDatasourceExtension(wDs).build();
    private static final WithSampleDataLoaded wSamples = WithSampleDataLoaded.builder(wDslContext)
            .createTablesIfNotExists()
            .addDataset(NotificationsRecordSamples.SAMPLE)
            .build();

    @RegisterExtension
    @SuppressWarnings("unused")
    static ChainedExtension chain = ChainedExtension.outer(wDs)
            .append(wDslContext)
            .append(wSamples)
            .register();

    private NotificationPersistencePort tested;

    @BeforeEach
    void setUp(DSLContext dsl) {
        tested = new NotificationPersistenceAdapter(Schedulers.immediate(), dsl, new ObjectMapper());
    }

    @Test
    void should_consume_notifications(DSLContext dsl) {
        assertThat(dsl.fetchCount(Notifications.NOTIFICATIONS)).isNotZero();

        Flux<ServerEvent> actuals = tested.consume(UserSamples.OBIWAN.id());

        StepVerifier.create(actuals)
                .assertNext(actual -> Assertions.assertAll(
                        () -> assertThat(actual.id()).isEqualTo("EV01HHA6PFESHHFK4YHT1T2HKHSR"),
                        () -> assertThat(actual.type()).isEqualTo(EventType.USER_NOTIFICATION)
                )).verifyComplete();

        assertThat(dsl.fetchCount(Notifications.NOTIFICATIONS)).isOne();
    }

    @Test
    void should_consume_dummy_notifications(DSLContext dsl) {
        assertThat(dsl.fetchCount(Notifications.NOTIFICATIONS)).isNotZero();

        Flux<ServerEvent> actuals = tested.consume(UserSamples.MWINDU.id());

        StepVerifier.create(actuals)
                .assertNext(actual -> Assertions.assertAll(
                        () -> assertThat(actual.id()).isEqualTo("EV01HHA6PFESHHFK4YHT1T2HKHSQ"),
                        () -> assertThat(actual.type()).isEqualTo(EventType.USER_NOTIFICATION)
                )).verifyComplete();

        assertThat(dsl.fetchCount(Notifications.NOTIFICATIONS)).isOne();
    }

    @Test
    void should_not_consume_notifications(DSLContext dsl) {
        assertThat(dsl.fetchCount(Notifications.NOTIFICATIONS)).isNotZero();

        Flux<ServerEvent> actuals = tested.consume(UserSamples.DSIDIOUS.id());

        StepVerifier.create(actuals).verifyComplete();

        assertThat(dsl.fetchCount(Notifications.NOTIFICATIONS)).isNotZero();
    }
}