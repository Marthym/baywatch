package fr.ght1pc9kc.baywatch.indexer.infra.adapters;

import fr.ght1pc9kc.baywatch.common.api.ScrapingEventHandler;
import fr.ght1pc9kc.baywatch.indexer.api.FeedIndexerService;
import fr.ght1pc9kc.baywatch.indexer.domain.handlers.IndexerEventHandler;
import lombok.experimental.Delegate;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "baywatch.indexer.enable", havingValue = "true")
public class IndexerEventHandlerAdapter implements ScrapingEventHandler {
    @Delegate
    private final ScrapingEventHandler delegate;

    public IndexerEventHandlerAdapter(FeedIndexerService indexerService) {
        this.delegate = new IndexerEventHandler(indexerService);
    }
}