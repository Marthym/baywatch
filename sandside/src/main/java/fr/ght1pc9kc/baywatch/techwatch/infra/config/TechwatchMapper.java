package fr.ght1pc9kc.baywatch.techwatch.infra.config;

import fr.ght1pc9kc.baywatch.common.domain.Hasher;
import fr.ght1pc9kc.baywatch.scraper.api.model.AtomFeed;
import fr.ght1pc9kc.baywatch.techwatch.api.model.Feed;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.net.URI;
import java.util.Set;

@Mapper(componentModel = "spring", imports = {URI.class, Set.class, Hasher.class})
public interface TechwatchMapper {
    @Mapping(target = "raw.id",
            expression = "java(Hasher.identify(atomFeed.link()))")
    @Mapping(source = "title", target = "raw.name")
    @Mapping(source = "title", target = "name")
    @Mapping(source = "description", target = "raw.description")
    @Mapping(source = "link", target = "raw.url")
    @Mapping(target = "tags", expression = "java(Set.of())")
    Feed getFeedFromAtom(AtomFeed atomFeed);
}
