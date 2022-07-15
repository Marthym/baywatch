package fr.ght1pc9kc.baywatch.indexer.domain;

import fr.ght1pc9kc.baywatch.common.api.model.EntitiesProperties;
import fr.ght1pc9kc.baywatch.indexer.domain.model.EntryDocument;
import fr.ght1pc9kc.baywatch.indexer.domain.model.FeedDocument;
import fr.ght1pc9kc.baywatch.indexer.domain.model.IndexableDocument;
import fr.ght1pc9kc.baywatch.indexer.domain.model.IndexableFeedEntry;
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

@Slf4j
@RequiredArgsConstructor
public final class FeedIndexerService {
    private static final PageRequest ALL_ORDER_BY_ID = PageRequest.of(Pagination.of(-1, -1,
            Sort.of(Order.desc(EntitiesProperties.ID))), Criteria.none());

    private final IndexBuilderPort indexBuilder;
    private final IndexableDataPort feedRepository;

    public Mono<Void> buildIndex() {
        Flux<IndexableDocument> feeds = feedRepository.listFeed().map(f -> new FeedDocument(
                f.id(), f.title(), f.description(), String.join(" ", f.tags())));

        Flux<EntryDocument> feedEntries = feedRepository.listEntries(ALL_ORDER_BY_ID)
                .bufferUntilChanged(IndexableFeedEntry::feed)
                .map(entries -> {
                    String feed = entries.get(0).feed();
                    StringBuilder titles = new StringBuilder();
                    StringBuilder descriptions = new StringBuilder();
                    for (IndexableFeedEntry entry : entries) {
                        titles.append(' ').append(entry.title());
                        descriptions.append(' ').append(entry.description());
                    }
                    return new EntryDocument(feed, titles.toString(), descriptions.toString());
                });

        return indexBuilder.write(Flux.concat(feeds, feedEntries));
    }
}
