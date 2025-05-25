package fr.ght1pc9kc.baywatch.indexer.domain;

import fr.ght1pc9kc.baywatch.common.api.model.EntitiesProperties;
import fr.ght1pc9kc.baywatch.indexer.api.FeedIndexerService;
import fr.ght1pc9kc.baywatch.indexer.domain.model.Indexable;
import fr.ght1pc9kc.baywatch.indexer.domain.ports.IndexBuilderPort;
import fr.ght1pc9kc.baywatch.indexer.domain.ports.IndexableDataPort;
import fr.ght1pc9kc.juery.api.Criteria;
import fr.ght1pc9kc.juery.api.PageRequest;
import fr.ght1pc9kc.juery.api.Pagination;
import fr.ght1pc9kc.juery.api.pagination.Order;
import fr.ght1pc9kc.juery.api.pagination.Sort;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.VisibleForTesting;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.SignalType;
import reactor.util.function.Tuples;

import java.net.URI;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import static java.util.Objects.isNull;

@Slf4j
@RequiredArgsConstructor
public final class FeedIndexerServiceImpl implements FeedIndexerService {
    private static final PageRequest ALL_ORDER_BY_ID = PageRequest.of(Pagination.of(-1, -1,
            Sort.of(Order.desc(EntitiesProperties.ID))), Criteria.none());

    private final IndexBuilderPort indexBuilder;
    private final IndexableDataPort indexableDataPort;

    @Getter(value = AccessLevel.PACKAGE, onMethod = @__({@VisibleForTesting}))
    private final Semaphore lock = new Semaphore(1);

    @Override
    public Mono<Void> buildIndex() {
        if (lock.tryAcquire()) {
            Flux<Indexable> indexables = indexableDataPort.listFeed().flatMap(f ->
                    indexableDataPort.listEntries(ALL_ORDER_BY_ID.withFilter(Criteria.property(EntitiesProperties.FEED_ID).eq(f.id())))
                            .collect(
                                    () -> Tuples.of(new StringBuilder(), new StringBuilder()),
                                    (acc, ie) -> {
                                        acc.getT1().append(' ').append(ie.title());
                                        acc.getT2().append(' ').append(ie.description());
                                    }).map(ie -> new Indexable(f.id(), f.title(), f.description(), f.link(), ie.getT1().toString(), ie.getT2().toString()))
            ).map(indexable -> {
                if (isNull(indexable.title())) {
                    return indexable.toBuilder()
                            .title(URI.create(indexable.link()).getHost())
                            .build();
                } else {
                    return indexable;
                }
            });

            return indexBuilder.write(indexables)
                    .doFinally(s -> {
                        if (s == SignalType.ON_ERROR) {
                            log.atError().log("Unable to build index !");
                        } else {
                            log.atInfo()
                                    .addArgument(s)
                                    .log("Feed index builder terminated with {}");
                        }
                        lock.release();
                    });
        } else {
            log.atDebug().log("Unable to build index, another indexer is running !");
            return Mono.empty().then();
        }
    }

    @Override
    public void waitAndShutdown() {
        try {
            if (!lock.tryAcquire(60, TimeUnit.SECONDS)) {
                log.error("Unable to terminate index builder after 60s !");
            }
        } catch (InterruptedException e) {
            log.warn("Abnormal indexer shutdown !", e);
            Thread.currentThread().interrupt();
        }
    }
}
