package fr.ght1pc9kc.baywatch.indexer.domain.ports;

import reactor.core.publisher.Flux;

public interface IndexSearcherPort {
    String escapeQuery(String terms);

    Flux<String> search(String terms);
}
