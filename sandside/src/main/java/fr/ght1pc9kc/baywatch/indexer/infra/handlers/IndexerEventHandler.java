package fr.ght1pc9kc.baywatch.indexer.infra.handlers;

import fr.ght1pc9kc.baywatch.indexer.api.FeedIndexerService;
import fr.ght1pc9kc.baywatch.scraper.api.ScrapingEventHandler;
import fr.ght1pc9kc.baywatch.scraper.api.model.ScrapResult;
import fr.ght1pc9kc.baywatch.scraper.api.model.ScrapingEventType;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.EnumSet;

@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "baywatch.indexer.enable", havingValue = "true")
public final class IndexerEventHandler implements ScrapingEventHandler {

    private final FeedIndexerService indexerService;

    @Override
    public Mono<Void> after(ScrapResult result) {
        return indexerService.buildIndex();
    }

    @Override
    public EnumSet<ScrapingEventType> eventTypes() {
        return EnumSet.of(ScrapingEventType.FEED_SCRAPING);
    }
}
