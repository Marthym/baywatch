package fr.ght1pc9kc.baywatch.indexer.domain;

import fr.ght1pc9kc.baywatch.indexer.api.FeedSearchService;
import fr.ght1pc9kc.baywatch.indexer.domain.ports.IndexSearcherPort;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;

@RequiredArgsConstructor
public class FeedSearchServiceImpl implements FeedSearchService {
    private final IndexSearcherPort indexSearcher;

    @Override
    public Flux<String> search(String terms) {
        return indexSearcher.search(indexSearcher.escapeQuery(terms));
    }
}
