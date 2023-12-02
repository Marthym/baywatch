package fr.ght1pc9kc.baywatch.scraper.infra.config;

import fr.ght1pc9kc.baywatch.common.domain.Hasher;
import fr.ght1pc9kc.baywatch.scraper.api.model.AtomEntry;
import fr.ght1pc9kc.baywatch.techwatch.api.model.News;
import org.jooq.tools.StringUtils;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.net.URI;
import java.util.Optional;

@Mapper(componentModel = "spring", imports = {
        Hasher.class, StringUtils.class, Optional.class, URI.class
})
public interface ScraperMapper {
    @Mapping(source = "raw.id", target = "id")
    @Mapping(source = "raw.title", target = "title")
    @Mapping(source = "raw.image", target = "image")
    @Mapping(source = "raw.description", target = "description")
    @Mapping(source = "raw.publication", target = "publication")
    @Mapping(source = "raw.link", target = "link")
    AtomEntry getAtomFromNews(News news);
}
