package fr.ght1pc9kc.baywatch.indexer.domain.ports;

import fr.ght1pc9kc.baywatch.indexer.domain.model.IndexableFeed;
import fr.ght1pc9kc.baywatch.indexer.domain.model.IndexableFeedEntry;
import fr.ght1pc9kc.juery.api.PageRequest;
import reactor.core.publisher.Flux;

public interface IndexableDataPort {
    Flux<IndexableFeed> listFeed();

    Flux<IndexableFeedEntry> listEntries(PageRequest pg);
}
