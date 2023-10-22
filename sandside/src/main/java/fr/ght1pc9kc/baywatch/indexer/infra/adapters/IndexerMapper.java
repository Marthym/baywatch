package fr.ght1pc9kc.baywatch.indexer.infra.adapters;

import fr.ght1pc9kc.baywatch.common.domain.Hasher;
import fr.ght1pc9kc.baywatch.indexer.domain.model.IndexableFeed;
import fr.ght1pc9kc.baywatch.indexer.domain.model.IndexableFeedEntry;
import fr.ght1pc9kc.baywatch.techwatch.api.model.RawNews;
import fr.ght1pc9kc.baywatch.techwatch.api.model.WebFeed;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import java.net.URI;
import java.util.Set;

@Mapper(componentModel = "spring",
        imports = {URI.class, Set.class, Hasher.class},
        unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface IndexerMapper {
    @Mapping(source = "reference", target = "id")
    @Mapping(source = "name", target = "title")
    @Mapping(target = "link", expression = "java(rf.location().toString())")
    IndexableFeed getIndexableFromFeed(WebFeed rf);

    IndexableFeedEntry getIndexableFromEntry(RawNews rn);
}
