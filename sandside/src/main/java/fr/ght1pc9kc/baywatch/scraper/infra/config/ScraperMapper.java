package fr.ght1pc9kc.baywatch.scraper.infra.config;

import fr.ght1pc9kc.baywatch.common.api.model.EntitiesProperties;
import fr.ght1pc9kc.baywatch.common.domain.DateUtils;
import fr.ght1pc9kc.baywatch.common.domain.Hasher;
import fr.ght1pc9kc.baywatch.dsl.tables.records.FeedsErrorsRecord;
import fr.ght1pc9kc.baywatch.scraper.api.model.AtomEntry;
import fr.ght1pc9kc.baywatch.scraper.api.model.ScrapingError;
import fr.ght1pc9kc.baywatch.techwatch.api.model.News;
import fr.ght1pc9kc.entity.api.Entity;
import org.jooq.Field;
import org.jooq.tools.StringUtils;
import org.mapstruct.InheritInverseConfiguration;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import java.net.URI;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;

import static fr.ght1pc9kc.baywatch.dsl.Tables.FEEDS_ERRORS;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE, imports = {
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

    @Mapping(target = "message", source = "feerLastLabel")
    @Mapping(target = "code", source = "feerLastStatus")
    @Mapping(target = "lastTime", source = "feerLastTime")
    @Mapping(target = "since", source = "feerSince")
    ScrapingError getScrapingError(FeedsErrorsRecord r);

    @InheritInverseConfiguration
    FeedsErrorsRecord getFeedErrorRecord(ScrapingError se);

    default LocalDateTime toLocalDateTime(Instant i) {
        return DateUtils.toLocalDateTime(i);
    }

    default Instant toInstant(LocalDateTime ldt) {
        return DateUtils.toInstant(ldt);
    }

    default FeedsErrorsRecord getFeedErrorRecord(Entity<ScrapingError> entity) {
        FeedsErrorsRecord feedErrorRecord = getFeedErrorRecord(entity.self());
        return feedErrorRecord.setFeerFeedId(entity.id());
    }

    default Entity<ScrapingError> getFeedErrorEntity(FeedsErrorsRecord r) {
        return Entity.identify(getScrapingError(r))
                .withId(r.getFeerFeedId());
    }

    Map<String, Field<?>> FEEDS_ERRORS_PROPERTIES_MAPPING = Map.of(
            EntitiesProperties.ID, FEEDS_ERRORS.FEER_FEED_ID
    );
}
