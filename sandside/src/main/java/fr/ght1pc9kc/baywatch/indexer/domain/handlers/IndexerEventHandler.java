package fr.ght1pc9kc.baywatch.indexer.domain.handlers;

import fr.ght1pc9kc.baywatch.common.api.ScrapingEventHandler;
import fr.ght1pc9kc.baywatch.indexer.api.FeedIndexerService;
import fr.ght1pc9kc.baywatch.scraper.api.model.ScrapResult;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

import java.util.Set;

@RequiredArgsConstructor
public final class IndexerEventHandler implements ScrapingEventHandler {

    private final FeedIndexerService indexerService;

    @Override
    public Mono<Void> after(ScrapResult result) {
        return indexerService.buildIndex();
    }

    @Override
    public Set<String> eventTypes() {
        return Set.of("FEED_SCRAPING");
    }
}
