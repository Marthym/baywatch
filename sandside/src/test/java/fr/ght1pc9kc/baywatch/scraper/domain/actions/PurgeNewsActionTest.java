package fr.ght1pc9kc.baywatch.scraper.domain.actions;

import fr.ght1pc9kc.baywatch.scraper.infra.config.ScraperApplicationProperties;
import fr.ght1pc9kc.baywatch.techwatch.domain.ports.NewsPersistencePort;
import fr.ght1pc9kc.baywatch.tests.samples.NewsSamples;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.Period;
import java.time.ZoneOffset;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class PurgeNewsActionTest {

    private PurgeNewsHandler tested;
    private NewsPersistencePort newsPersistenceMock;

    @BeforeEach
    void setUp() {
        final ScraperApplicationProperties scraperProperties = new ScraperApplicationProperties(
                true, Duration.ofHours(1), Period.ofMonths(5), Duration.ofSeconds(10),
                new ScraperApplicationProperties.DnsProperties(Duration.ofSeconds(10))
        );
        newsPersistenceMock = mock(NewsPersistencePort.class);
        tested = new PurgeNewsHandler(newsPersistenceMock, scraperProperties)
                .clock(Clock.fixed(Instant.parse("2020-12-18T22:42:42Z"), ZoneOffset.UTC));

        when(newsPersistenceMock.listId(any())).thenReturn(testDataForPersistenceList());
        when(newsPersistenceMock.delete(any())).thenReturn(Mono.just(4));
    }

    @Test
    void should_purge_news() {
        StepVerifier.create(tested.before()).verifyComplete();

        verify(newsPersistenceMock).delete(List.of(
                "de2648a666d829d4ee66ffa1a0bc141c7f888ed89d319dc212f83d4f380271f1",
                "bd32550e3963aed4aa6fead627ddc694e31a91d0e7b85cfa68e1c5fd7a4a9277",
                "3fbe6f22297571d2a4b1f35c8c08fe3b2aaa17c155b4c3b2fc842b3d188f55e9"
        ));
    }

    private Flux<String> testDataForPersistenceList() {
        return Flux.just(
                NewsSamples.A_NEW_HOPE.id(),
                NewsSamples.ORDER_66.id(),
                NewsSamples.MAY_THE_FORCE.id()
        );
    }
}