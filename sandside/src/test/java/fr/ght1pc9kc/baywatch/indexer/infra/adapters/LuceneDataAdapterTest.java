package fr.ght1pc9kc.baywatch.indexer.infra.adapters;

import fr.ght1pc9kc.baywatch.indexer.infra.config.IndexerProperties;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import reactor.test.StepVerifier;

@Slf4j
class LuceneDataAdapterTest {
    private LuceneDataAdapter tested;

    @BeforeEach
    void setUp() {
        tested = new LuceneDataAdapter(new IndexerProperties(true, "/home/marthym/.baywatch/feedidx"));
    }

    @Test
    void should_search_in_index() {
        StepVerifier.create(tested.search("\"spring boot\""))
                .expectNextCount(4)
                .verifyComplete();
    }
}