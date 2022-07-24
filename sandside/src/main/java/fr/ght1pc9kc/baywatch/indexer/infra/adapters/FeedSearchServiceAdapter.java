package fr.ght1pc9kc.baywatch.indexer.infra.adapters;

import fr.ght1pc9kc.baywatch.indexer.api.FeedSearchService;
import fr.ght1pc9kc.baywatch.indexer.domain.FeedSearchServiceImpl;
import fr.ght1pc9kc.baywatch.indexer.domain.ports.IndexSearcherPort;
import lombok.experimental.Delegate;
import org.springframework.stereotype.Service;

@Service
public class FeedSearchServiceAdapter implements FeedSearchService {
    @Delegate
    private final FeedSearchService delegate;

    public FeedSearchServiceAdapter(IndexSearcherPort indexSearcher) {
        this.delegate = new FeedSearchServiceImpl(indexSearcher);
    }
}
