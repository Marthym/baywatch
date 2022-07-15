package fr.ght1pc9kc.baywatch.indexer.infra.adapters;

import fr.ght1pc9kc.baywatch.common.domain.Hasher;
import fr.ght1pc9kc.baywatch.indexer.domain.model.IndexableFeed;
import fr.ght1pc9kc.baywatch.indexer.domain.model.IndexableFeedEntry;
import fr.ght1pc9kc.baywatch.techwatch.api.model.RawFeed;
import fr.ght1pc9kc.baywatch.techwatch.api.model.RawNews;
import org.mapstruct.Mapper;

import java.net.URI;
import java.util.Set;

@Mapper(componentModel = "spring", imports = {URI.class, Set.class, Hasher.class})
public interface IndexerMapper {
    IndexableFeed getIndexableFromFeed(RawFeed rf);

    IndexableFeedEntry getIndexableFromEntry(RawNews rn);
}
