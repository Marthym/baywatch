package fr.ght1pc9kc.baywatch.indexer.domain;

import fr.ght1pc9kc.baywatch.indexer.api.FeedIndexerService;
import fr.ght1pc9kc.baywatch.indexer.domain.model.Indexable;
import fr.ght1pc9kc.baywatch.indexer.domain.model.IndexableFeed;
import fr.ght1pc9kc.baywatch.indexer.domain.model.IndexableFeedEntry;
import fr.ght1pc9kc.baywatch.indexer.domain.ports.IndexBuilderPort;
import fr.ght1pc9kc.baywatch.indexer.domain.ports.IndexableDataPort;
import fr.ght1pc9kc.juery.api.PageRequest;
import org.assertj.core.api.Assertions;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class FeedIndexerServiceImplTest {
    private FeedIndexerService tested;
    private IndexBuilderPort indexBuilderPort;
    private IndexableDataPort indexableDataPort;

    @BeforeEach
    void setUp() {
        indexBuilderPort = spy(new IndexBuilderPort() {
            @Override
            public Mono<Void> write(Flux<Indexable> documents) {
                return documents.then();
            }
        });

        indexableDataPort = mock(IndexableDataPort.class);

        tested = new FeedIndexerServiceImpl(indexBuilderPort, indexableDataPort);
    }

    @Test
    void should_build_index() {
        IndexableFeed feed = new IndexableFeed(
                "001",
                "Star Wars Characters",
                "A feed of iconic Star Wars characters",
                "https://starwars.com/characters",
                "George Lucas",
                List.of("Science Fiction", "Movies", "Characters")
        );

        IndexableFeedEntry entry = new IndexableFeedEntry(
                "002",
                "Luke Skywalker",
                "The central hero of the original trilogy, a Jedi Knight."
        );

        when(indexableDataPort.listFeed()).thenReturn(Flux.just(feed));
        when(indexableDataPort.listEntries(any(PageRequest.class))).thenReturn(Flux.just(entry));

        StepVerifier.create(tested.buildIndex())
                .verifyComplete();

        verify(indexableDataPort, times(1)).listFeed();
        verify(indexableDataPort, times(1)).listEntries(any());
        verify(indexBuilderPort, times(1)).write(any());
    }

    @Test
    void should_build_index_without_feed_name() {
        IndexableFeed feed = new IndexableFeed(
                "001",
                null,
                "A feed of iconic Star Wars characters",
                "https://starwars.com/characters",
                "George Lucas",
                List.of("Science Fiction", "Movies", "Characters")
        );

        IndexableFeedEntry entry = new IndexableFeedEntry(
                "002",
                null,
                "The central hero of the original trilogy, a Jedi Knight."
        );

        when(indexableDataPort.listFeed()).thenReturn(Flux.just(feed));
        when(indexableDataPort.listEntries(any(PageRequest.class))).thenReturn(Flux.just(entry));

        StepVerifier.create(tested.buildIndex())
                .verifyComplete();

        verify(indexableDataPort, times(1)).listFeed();
        verify(indexableDataPort, times(1)).listEntries(any());
        verify(indexBuilderPort, times(1)).write(any());
    }

    @Test
    @SuppressWarnings("ResultOfMethodCallIgnored")
    void should_ignore_on_lock() {
        when(indexableDataPort.listFeed()).thenReturn(Flux.empty());
        when(indexableDataPort.listEntries(any(PageRequest.class))).thenReturn(Flux.empty());

        ((FeedIndexerServiceImpl) tested).getLock().tryAcquire();
        StepVerifier.create(tested.buildIndex())
                .verifyComplete();

        verify(indexableDataPort, never()).listFeed();
    }

    @Test
    @SuppressWarnings("ResultOfMethodCallIgnored")
    void should_wait_on_shutdown() {
        ((FeedIndexerServiceImpl) tested).getLock().tryAcquire();
        try (ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor()) {
            scheduler.schedule(() ->
                    ((FeedIndexerServiceImpl) tested).getLock().release(), 100, TimeUnit.MILLISECONDS);
            tested.waitAndShutdown();
            Assertions.assertThat(((FeedIndexerServiceImpl) tested).getLock().availablePermits()).isZero();
        }
    }

    @Test
    @SuppressWarnings("ResultOfMethodCallIgnored")
    void should_interrupt_on_shutdown() {
        ((FeedIndexerServiceImpl) tested).getLock().tryAcquire();
        Thread waiter = new Thread(tested::waitAndShutdown);
        waiter.start();
        waiter.interrupt();
        Awaitility.await()
                .atMost(Duration.ofSeconds(1))
                .until(waiter::isInterrupted);
    }

    @Test
    void should_handle_error_on_build_index() {
        when(indexableDataPort.listFeed()).thenReturn(Flux.error(new RuntimeException("Test error")));

        StepVerifier.create(tested.buildIndex())
                .verifyError(RuntimeException.class);

        verify(indexableDataPort, times(1)).listFeed();
        verify(indexBuilderPort, times(1)).write(any());
    }

    @Test
    void should_use_host_when_title_null() {

        IndexableFeed feed = new IndexableFeed(
                "001",
                null,
                "Site officiel de l'Empire Galactique",
                "https://empire-galactique.sw/feed",
                "Dark Vador",
                List.of("Empire", "Force", "Dark Side")
        );

        when(indexableDataPort.listFeed()).thenReturn(Flux.just(feed));
        when(indexableDataPort.listEntries(any(PageRequest.class))).thenReturn(Flux.empty());

        StepVerifier.create(tested.buildIndex())
                .verifyComplete();

        verify(indexBuilderPort, times(1)).write(any());
    }

}