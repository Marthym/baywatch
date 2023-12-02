package fr.ght1pc9kc.baywatch.indexer.infra.adapters;

import fr.ght1pc9kc.baywatch.indexer.api.FeedIndexerService;
import fr.ght1pc9kc.baywatch.indexer.domain.FeedIndexerServiceImpl;
import fr.ght1pc9kc.baywatch.indexer.domain.ports.IndexBuilderPort;
import fr.ght1pc9kc.baywatch.indexer.domain.ports.IndexableDataPort;
import jakarta.annotation.PreDestroy;
import lombok.SneakyThrows;
import lombok.experimental.Delegate;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@ConditionalOnProperty(name = "baywatch.indexer.enable", havingValue = "true")
public class FeedIndexerServiceAdapter implements FeedIndexerService {
    @Delegate
    private final FeedIndexerServiceImpl delegate;

    public FeedIndexerServiceAdapter(
            IndexBuilderPort indexBuilderPort, IndexableDataPort indexableDataPort) {
        this.delegate = new FeedIndexerServiceImpl(indexBuilderPort, indexableDataPort);
    }

    @PreDestroy
    @SneakyThrows
    void waitOnTerminate() {
        delegate.waitAndShutdown();
    }
}
