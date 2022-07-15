package fr.ght1pc9kc.baywatch.indexer.domain.ports;

import fr.ght1pc9kc.baywatch.indexer.domain.model.IndexableDocument;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface IndexBuilderPort {
    Mono<Void> write(Flux<IndexableDocument> documents);
}
