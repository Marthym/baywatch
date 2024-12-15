package fr.ght1pc9kc.baywatch.indexer.infra.handlers;

import fr.ght1pc9kc.baywatch.indexer.api.FeedIndexerService;
import fr.ght1pc9kc.baywatch.scraper.api.model.ScrapResult;
import fr.ght1pc9kc.baywatch.scraper.api.model.ScrapingEventType;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.List;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class IndexerEventHandlerTest {

    private final FeedIndexerService feedIndexerService = mock(FeedIndexerService.class);
    private final IndexerEventHandler tested = new IndexerEventHandler(feedIndexerService);

    @Test
    void should_handle_event() {
        doReturn(Mono.empty().then()).when(feedIndexerService).buildIndex();
        Assertions.assertThat(tested.eventTypes()).contains(ScrapingEventType.FEED_SCRAPING);

        StepVerifier.create(tested.after(new ScrapResult(1, List.of())))
                .verifyComplete();

        verify(feedIndexerService).buildIndex();
    }
}