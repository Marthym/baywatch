package fr.ght1pc9kc.baywatch.indexer.domain;

import fr.ght1pc9kc.baywatch.common.api.model.EntitiesProperties;
import fr.ght1pc9kc.baywatch.indexer.domain.model.Indexable;
import fr.ght1pc9kc.baywatch.indexer.domain.ports.IndexBuilderPort;
import fr.ght1pc9kc.baywatch.indexer.domain.ports.IndexableDataPort;
import fr.ght1pc9kc.juery.api.Criteria;
import fr.ght1pc9kc.juery.api.PageRequest;
import fr.ght1pc9kc.juery.api.Pagination;
import fr.ght1pc9kc.juery.api.pagination.Order;
import fr.ght1pc9kc.juery.api.pagination.Sort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuples;

@Slf4j
@RequiredArgsConstructor
public final class FeedIndexerService {
    private static final PageRequest ALL_ORDER_BY_ID = PageRequest.of(Pagination.of(-1, -1,
            Sort.of(Order.desc(EntitiesProperties.ID))), Criteria.none());

    private final IndexBuilderPort indexBuilder;
    private final IndexableDataPort feedRepository;

    public Mono<Void> buildIndex() {
        Flux<Indexable> indexables = feedRepository.listFeed().flatMap(f ->
                feedRepository.listEntries(ALL_ORDER_BY_ID.withFilter(Criteria.property(EntitiesProperties.FEED_ID).eq(f.id())))
                        .collect(() -> Tuples.of(new StringBuilder(), new StringBuilder()), (acc, ie) -> {
                            acc.getT1().append(' ').append(ie.title());
                            acc.getT2().append(' ').append(ie.description());
                        }).map(ie -> new Indexable(f.id(), f.title(), f.description(), f.link(), ie.getT1().toString(), ie.getT2().toString())));

        return indexBuilder.write(indexables);
    }
}
