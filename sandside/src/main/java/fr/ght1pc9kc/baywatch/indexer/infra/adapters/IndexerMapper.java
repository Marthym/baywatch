package fr.ght1pc9kc.baywatch.indexer.infra.adapters;

import fr.ght1pc9kc.baywatch.admin.api.model.RawFeed;
import fr.ght1pc9kc.baywatch.common.domain.Hasher;
import fr.ght1pc9kc.baywatch.indexer.domain.model.IndexableFeed;
import fr.ght1pc9kc.baywatch.indexer.domain.model.IndexableFeedEntry;
import fr.ght1pc9kc.baywatch.techwatch.api.model.RawNews;
import fr.ght1pc9kc.baywatch.techwatch.api.model.WebFeed;
import fr.ght1pc9kc.entity.api.Entity;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

import java.net.URI;
import java.util.List;
import java.util.Set;

@Mapper(componentModel = "spring",
        imports = {URI.class, Set.class, Hasher.class},
        unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface IndexerMapper {
    default IndexableFeed getIndexableFromFeed(Entity<WebFeed> rf) {
        return new IndexableFeed(rf.id(), rf.self().name(), rf.self().description(),
                rf.self().location().toString(), null, List.copyOf(rf.self().tags()));
    }

    default IndexableFeed toIndexableFeed(Entity<RawFeed> rawFeedEntity) {
        return new IndexableFeed(
                rawFeedEntity.id(),
                rawFeedEntity.self().name(),
                rawFeedEntity.self().description(),
                rawFeedEntity.self().url().toString(),
                null, List.of()
        );
    }

    IndexableFeedEntry getIndexableFromEntry(RawNews rn);
}
