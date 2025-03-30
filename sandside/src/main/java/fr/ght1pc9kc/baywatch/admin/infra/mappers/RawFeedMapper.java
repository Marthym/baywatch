package fr.ght1pc9kc.baywatch.admin.infra.mappers;

import fr.ght1pc9kc.baywatch.admin.api.model.RawFeed;
import fr.ght1pc9kc.baywatch.common.api.model.FeedMeta;
import fr.ght1pc9kc.baywatch.techwatch.api.model.WebFeed;
import fr.ght1pc9kc.entity.api.Entity;
import org.mapstruct.InheritInverseConfiguration;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import java.time.Instant;

@Mapper(componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface RawFeedMapper {
    default Entity<RawFeed> toRawFeed(Entity<WebFeed> webFeed) {
        return Entity.identify(toRawFeed(webFeed.self()).toBuilder()
                        .lastETag(webFeed.meta(FeedMeta.ETag).orElse(null))
                        .lastWatch(webFeed.meta(FeedMeta.updated, Instant.class).orElse(null))
                        .build())
                .withId(webFeed.id());
    }

    @Mapping(target = "url", source = "location")
    @Mapping(target = "lastWatch", ignore = true)
    @Mapping(target = "lastETag", ignore = true)
    RawFeed toRawFeed(WebFeed webFeed);

    @InheritInverseConfiguration
    WebFeed toWebFeed(RawFeed rawFeed);

    default Entity<WebFeed> toWebFeed(Entity<RawFeed> rawFeed) {
        return Entity.identify(toWebFeed(rawFeed.self()))
                .meta(FeedMeta.ETag, rawFeed.self().lastETag())
                .meta(FeedMeta.updated, rawFeed.self().lastWatch())
                .withId(rawFeed.id());
    }
}
