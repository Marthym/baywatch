package fr.ght1pc9kc.baywatch.indexer.infra.adapters;

import fr.ght1pc9kc.baywatch.indexer.domain.model.IndexableFeed;
import fr.ght1pc9kc.baywatch.indexer.domain.model.IndexableFeedEntry;
import fr.ght1pc9kc.baywatch.indexer.domain.ports.IndexableDataPort;
import fr.ght1pc9kc.baywatch.techwatch.api.SystemMaintenanceService;
import fr.ght1pc9kc.juery.api.PageRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

@Component
@RequiredArgsConstructor
public class IndexableDataAdapter implements IndexableDataPort {
    private final SystemMaintenanceService systemMaintenanceService;
    private final IndexerMapper mapper;

    @Override
    public Flux<IndexableFeed> listFeed() {
        return systemMaintenanceService.feedList()
                .map(mapper::getIndexableFromFeed);
    }

    @Override
    public Flux<IndexableFeedEntry> listEntries(PageRequest pg) {
        return systemMaintenanceService.newsList(pg)
                .map(mapper::getIndexableFromEntry);
    }
}
