package fr.ght1pc9kc.baywatch.indexer.api;

import reactor.core.publisher.Mono;

public interface FeedIndexerService {
    Mono<Void> buildIndex();

    void waitAndShutdown();
}
