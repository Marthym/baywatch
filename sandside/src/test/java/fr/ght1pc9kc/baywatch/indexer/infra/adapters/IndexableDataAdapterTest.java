package fr.ght1pc9kc.baywatch.indexer.infra.adapters;

import fr.ght1pc9kc.baywatch.admin.api.FeedManagementService;
import fr.ght1pc9kc.baywatch.admin.api.model.RawFeed;
import fr.ght1pc9kc.baywatch.indexer.domain.model.IndexableFeed;
import fr.ght1pc9kc.baywatch.indexer.domain.model.IndexableFeedEntry;
import fr.ght1pc9kc.baywatch.techwatch.api.SystemMaintenanceService;
import fr.ght1pc9kc.baywatch.tests.samples.NewsSamples;
import fr.ght1pc9kc.entity.api.Entity;
import fr.ght1pc9kc.juery.api.PageRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.net.URI;
import java.time.Instant;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class IndexableDataAdapterTest {
    @Mock
    private FeedManagementService feedManagementService;
    @Mock
    private SystemMaintenanceService systemMaintenanceService;
    @Mock
    private IndexerMapper mapper;

    private IndexableDataAdapter tested;

    @BeforeEach
    void setUp() {
        tested = new IndexableDataAdapter(feedManagementService, systemMaintenanceService, mapper);
    }

    @Test
    void should_list_feeds() {
        IndexableFeed expected = new IndexableFeed(
                "001",
                "Star Wars Feed",
                "A feed of information about iconic Star Wars content.",
                "https://starwars.com/feed",
                "George Lucas",
                List.of("Movies", "Sci-Fi", "Fiction")
        );
        RawFeed rawFeed = RawFeed.builder()
                .name("Star Wars News")
                .description("Latest updates and news about the Star Wars universe.")
                .url(URI.create("https://starwars.com/news"))
                .icon(URI.create("https://starwars.com/icon.png"))
                .lastWatch(Instant.parse("2023-10-01T10:10:10Z"))
                .lastETag("123456789abcdef")
                .build();

        when(feedManagementService.find(any())).thenReturn(Flux.just(Entity.identify(rawFeed).withId("42")));
        when(mapper.toIndexableFeed(any())).thenReturn(expected);

        Flux<IndexableFeed> result = tested.listFeed();

        StepVerifier.create(result)
                .expectNext(expected)
                .verifyComplete();
    }

    @Test
    void should_list_entries() {
        IndexableFeedEntry expected = new IndexableFeedEntry(
                NewsSamples.A_NEW_HOPE.id(),
                NewsSamples.A_NEW_HOPE.title(),
                NewsSamples.A_NEW_HOPE.description()
        );

        when(systemMaintenanceService.newsList(any())).thenReturn(Flux.just(NewsSamples.A_NEW_HOPE));
        when(mapper.getIndexableFromEntry(any())).thenReturn(expected);

        Flux<IndexableFeedEntry> result = tested.listEntries(PageRequest.all());

        StepVerifier.create(result)
                .expectNext(expected)
                .verifyComplete();
    }
}