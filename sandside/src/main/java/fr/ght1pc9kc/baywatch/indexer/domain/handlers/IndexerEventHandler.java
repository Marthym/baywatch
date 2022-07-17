package fr.ght1pc9kc.baywatch.indexer.domain.handlers;

import fr.ght1pc9kc.baywatch.common.api.EventHandler;
import fr.ght1pc9kc.baywatch.indexer.api.FeedIndexerService;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

import java.util.Set;

@RequiredArgsConstructor
public final class IndexerEventHandler implements EventHandler {

    private final FeedIndexerService indexerService;

    @Override
    public Mono<Void> after(int persisted) {
        return indexerService.buildIndex();
    }

    @Override
    public Set<String> eventTypes() {
        return Set.of("FEED_SCRAPING");
    }
}
