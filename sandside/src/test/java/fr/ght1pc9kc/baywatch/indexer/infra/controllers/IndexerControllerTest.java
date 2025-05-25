package fr.ght1pc9kc.baywatch.indexer.infra.controllers;

import fr.ght1pc9kc.baywatch.indexer.api.FeedIndexerService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class IndexerControllerTest {

    private FeedIndexerService indexerService;
    private IndexerController indexerController;

    @BeforeEach
    void setUp() {
        indexerService = Mockito.mock(FeedIndexerService.class);

        indexerController = new IndexerController(indexerService);
        when(indexerService.buildIndex()).thenReturn(Mono.empty());
    }

    @Test
    void should_trigger_feed_index_build_successfully() {
        Mono<Void> result = indexerController.indexerBuildFeedIndex();

        StepVerifier.create(result)
                .verifyComplete();

        verify(indexerService, times(1)).buildIndex();
    }

    @Test
    void should_log_error_when_feed_index_fails() {
        RuntimeException mockException = new RuntimeException("Simulated failure");
        when(indexerService.buildIndex()).thenReturn(Mono.error(mockException));

        Mono<Void> result = indexerController.indexerBuildFeedIndex();

        StepVerifier.create(result)
                .expectError(RuntimeException.class)
                .verify();

        verify(indexerService, times(1)).buildIndex();
    }
}