package fr.ght1pc9kc.baywatch.indexer.infra.adapters;

import fr.ght1pc9kc.baywatch.indexer.domain.FeedIndexerService;
import fr.ght1pc9kc.baywatch.indexer.domain.ports.IndexBuilderPort;
import fr.ght1pc9kc.baywatch.indexer.domain.ports.IndexableDataPort;
import fr.ght1pc9kc.baywatch.indexer.infra.config.IndexerProperties;
import fr.ght1pc9kc.baywatch.security.api.AuthenticationFacade;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.DependsOn;
import org.springframework.scheduling.concurrent.CustomizableThreadFactory;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@DependsOn({"flyway", "flywayInitializer"})
public class FeedIndexerServiceAdapter {
    private final FeedIndexerService delegate;
    private final IndexerProperties properties;

    private final Semaphore lock = new Semaphore(1);
    private final ExecutorService executor = Executors.newSingleThreadExecutor(
            new CustomizableThreadFactory("indexer-"));

    public FeedIndexerServiceAdapter(IndexBuilderPort indexBuilderPort, IndexableDataPort indexableDataPort, IndexerProperties properties) {
        this.delegate = new FeedIndexerService(indexBuilderPort, indexableDataPort);
        this.properties = properties;
    }

    @PostConstruct
    void buildIndex() {
        if (properties.enable() && lock.tryAcquire()) {
            executor.execute(() -> delegate.buildIndex()
                    .doFinally(signal -> lock.release())
                    .contextWrite(AuthenticationFacade.withSystemAuthentication())
                    .subscribe());
        }
    }

    @PreDestroy
    @SneakyThrows
    void waitOnTerminate() {
        executor.shutdown();
        if (!executor.awaitTermination(60, TimeUnit.SECONDS)) {
            log.warn("Unable to stop indexer gracefully !");
            executor.shutdownNow();
        }
    }
}
