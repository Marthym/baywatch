package fr.ght1pc9kc.baywatch.domain.scrapper.actions;

import fr.ght1pc9kc.baywatch.api.NewsPersistencePort;
import fr.ght1pc9kc.baywatch.api.model.Flags;
import fr.ght1pc9kc.baywatch.api.model.RawNews;
import fr.ght1pc9kc.baywatch.api.model.State;
import fr.ght1pc9kc.baywatch.dsl.tables.records.NewsRecord;
import fr.ght1pc9kc.baywatch.infra.samples.NewsRecordSamples;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class PurgeNewsActionTest {

    private PurgeNewsAction tested;

    private NewsPersistencePort newsPersistenceMock;

    @BeforeEach
    void setUp() {
        newsPersistenceMock = mock(NewsPersistencePort.class);
        tested = new PurgeNewsAction(
                newsPersistenceMock,
                Clock.fixed(Instant.parse("2020-12-18T22:42:42Z"), ZoneOffset.UTC));

        when(newsPersistenceMock.list(any())).thenReturn(testDataForPersistenceList());
        when(newsPersistenceMock.listState(any())).thenReturn(testDataForPersistenceListState());
        when(newsPersistenceMock.delete(any())).thenReturn(Mono.just(4));
    }

    @Test
    void should_purge_news() {
        tested.call().block();
        verify(newsPersistenceMock).delete(eq(List.of(
                "24abc4ad15dc0ab7824f0192b78cc786a7e57f10c0a50fc0721ac1cc3cd162fc",
                "60b59b7b9b35aa3805af8cf300fcb289055bbc78b012921f231aab5d5921a39c",
                "e35d5a3be1d1fbf1363fbeb1bca2ca248da0dcdfe41b88beb80e9548d9a10c8f",
                "0479255273c08312a67145eec4852293345555eb1145ce0b4243c8314a85ba0c"
        )));
    }

    private Flux<RawNews> testDataForPersistenceList() {
        return Flux.fromStream(
                NewsRecordSamples.SAMPLE.records().subList(0, 5).stream()
                        .map(r -> RawNews.builder().id(r.getNewsId()).link(URI.create(r.getNewsLink())).build())
        );
    }

    private Flux<Entry<String, State>> testDataForPersistenceListState() {
        NewsRecord staredRecord = NewsRecordSamples.SAMPLE.records().get(2);
        return Flux.just(Map.entry(staredRecord.getNewsId(), State.of(Flags.STAR)));
    }
}