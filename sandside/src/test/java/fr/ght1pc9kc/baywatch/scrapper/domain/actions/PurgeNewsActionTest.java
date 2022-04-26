package fr.ght1pc9kc.baywatch.scrapper.domain.actions;

import fr.ght1pc9kc.baywatch.common.api.model.Entity;
import fr.ght1pc9kc.baywatch.common.infra.mappers.BaywatchMapper;
import fr.ght1pc9kc.baywatch.dsl.tables.records.NewsRecord;
import fr.ght1pc9kc.baywatch.scrapper.infra.config.ScrapperProperties;
import fr.ght1pc9kc.baywatch.techwatch.api.model.Flags;
import fr.ght1pc9kc.baywatch.techwatch.api.model.News;
import fr.ght1pc9kc.baywatch.techwatch.api.model.State;
import fr.ght1pc9kc.baywatch.techwatch.domain.ports.NewsPersistencePort;
import fr.ght1pc9kc.baywatch.techwatch.domain.ports.StatePersistencePort;
import fr.ght1pc9kc.baywatch.tests.samples.infra.NewsRecordSamples;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.Period;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class PurgeNewsActionTest {

    private static final BaywatchMapper mapper = Mappers.getMapper(BaywatchMapper.class);

    private PurgeNewsHandler tested;
    private NewsPersistencePort newsPersistenceMock;

    @BeforeEach
    void setUp() {
        newsPersistenceMock = mock(NewsPersistencePort.class);
        StatePersistencePort statePersistenceMock = mock(StatePersistencePort.class);
        tested = new PurgeNewsHandler(newsPersistenceMock, statePersistenceMock,
                new ScrapperProperties(true, Duration.ofHours(1), Duration.ofSeconds(10), Period.ofMonths(5),
                        Set.of("http", "https")))
                .clock(Clock.fixed(Instant.parse("2020-12-18T22:42:42Z"), ZoneOffset.UTC));

        when(newsPersistenceMock.list(any())).thenReturn(testDataForPersistenceList());
        when(statePersistenceMock.list(any())).thenReturn(testDataForPersistenceListState());
        when(newsPersistenceMock.delete(any())).thenReturn(Mono.just(4));
    }

    @Test
    void should_purge_news() {
        tested.before().block();
        verify(newsPersistenceMock).delete(List.of(
                "22f530b91e1dac4854cd3273b1ca45784e08a00fac25ca01792c6989db152fc0",
                "1fff2b3142d5d27677567a0da6811c09a428e7636f169d77006dede02ee6cec0",
                "900cf7d10afd3c1584d6d3122743a86c0315fde7d8acbe3a585a2cb7c301807c",
                "8a1161a7d2fc70fd5e865d3394eddfc0dbad40a792973f9dad50ff62afdb088b"
        ));
    }

    private Flux<News> testDataForPersistenceList() {
        return Flux.fromStream(
                NewsRecordSamples.SAMPLE.records().subList(0, 5).stream()
                        .map(mapper::recordToNews)
        );
    }

    private Flux<Entity<State>> testDataForPersistenceListState() {
        NewsRecord staredRecord = NewsRecordSamples.SAMPLE.records().get(2);
        return Flux.just(Entity.identify(staredRecord.getNewsId(), State.of(Flags.SHARED)));
    }
}