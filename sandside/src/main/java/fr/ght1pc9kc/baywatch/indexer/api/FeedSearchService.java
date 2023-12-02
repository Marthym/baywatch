package fr.ght1pc9kc.baywatch.indexer.api;

import reactor.core.publisher.Flux;

public interface FeedSearchService {
    Flux<String> search(String terms);
}
