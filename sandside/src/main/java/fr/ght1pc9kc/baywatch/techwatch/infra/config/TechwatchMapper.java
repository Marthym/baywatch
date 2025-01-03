package fr.ght1pc9kc.baywatch.techwatch.infra.config;

import fr.ght1pc9kc.baywatch.common.domain.DateUtils;
import fr.ght1pc9kc.baywatch.common.domain.Hasher;
import fr.ght1pc9kc.baywatch.dsl.tables.records.FeedsRecord;
import fr.ght1pc9kc.baywatch.scraper.api.model.AtomFeed;
import fr.ght1pc9kc.baywatch.techwatch.api.model.ImageProxyProperties;
import fr.ght1pc9kc.baywatch.techwatch.api.model.WebFeed;
import fr.ght1pc9kc.entity.api.Entity;
import org.jooq.Record;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.net.URI;
import java.nio.ByteBuffer;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.HexFormat;
import java.util.Set;

import static fr.ght1pc9kc.baywatch.common.api.model.FeedMeta.ETag;
import static fr.ght1pc9kc.baywatch.common.api.model.FeedMeta.createdBy;
import static fr.ght1pc9kc.baywatch.common.api.model.FeedMeta.updated;
import static fr.ght1pc9kc.baywatch.dsl.tables.Feeds.FEEDS;
import static fr.ght1pc9kc.baywatch.dsl.tables.FeedsUsers.FEEDS_USERS;

@Mapper(componentModel = "spring",
        imports = {URI.class, Set.class, Hasher.class, ByteBuffer.class, HexFormat.class, Instant.class})
public interface TechwatchMapper {
    @Mapping(source = "title", target = "name")
    @Mapping(source = "link", target = "location")
    @Mapping(target = "tags", expression = "java(Set.of())")
    WebFeed getFeedFromAtom(AtomFeed atomFeed);

    @Mapping(target = "signingKey",
            expression = "java(ByteBuffer.wrap(HexFormat.of().parseHex(config.signingKey())))")
    @Mapping(target = "signingSalt",
            expression = "java(ByteBuffer.wrap(HexFormat.of().parseHex(config.signingSalt())))")
    ImageProxyProperties toProperties(ImageProxyConfig config);

    default Entity<WebFeed> recordToFeed(Record r) {
        Set<String> tags = Set.of();
        String name = r.get(FEEDS.FEED_NAME);

        String owner = (r.indexOf(FEEDS_USERS.FEUS_USER_ID) >= 0 && r.get(FEEDS_USERS.FEUS_USER_ID) != null)
                ? r.get(FEEDS_USERS.FEUS_USER_ID) : null;

        Instant lastPublication = (r.indexOf(FEEDS.FEED_LAST_WATCH) >= 0 && r.get(FEEDS.FEED_LAST_WATCH) != null)
                ? DateUtils.toInstant(r.get(FEEDS.FEED_LAST_WATCH, LocalDateTime.class)) : Instant.EPOCH;

        String lastETag = (r.indexOf(FEEDS.FEED_LAST_ETAG) >= 0 && r.get(FEEDS.FEED_LAST_ETAG) != null)
                ? r.get(FEEDS.FEED_LAST_ETAG) : null;

        assert lastPublication != null : "Last publication date cannot be null !";

        WebFeed webFeed = WebFeed.builder()
                .name(name)
                .description(r.get(FEEDS.FEED_DESCRIPTION))
                .location(URI.create(r.get(FEEDS.FEED_URL)))
                .tags(tags)
                .build();

        return Entity.identify(webFeed)
                .meta(createdBy, owner)
                .meta(updated, lastPublication)
                .meta(ETag, lastETag)
                .withId(r.get(FEEDS.FEED_ID));
    }

    default FeedsRecord feedToFeedsRecord(Entity<WebFeed> feed) {
        FeedsRecord feedsRecord = FEEDS.newRecord();
        feedsRecord.setFeedId(feed.id());
        feed.meta(updated, Instant.class).map(DateUtils::toLocalDateTime)
                .ifPresent(feedsRecord::setFeedLastWatch);
        feed.meta(ETag).ifPresent(feedsRecord::setFeedLastEtag);
        if (feed.self().name() != null) {
            feedsRecord.setFeedName(feed.self().name());
        }
        if (feed.self().description() != null) {
            feedsRecord.setFeedDescription(feed.self().description());
        }
        feedsRecord.setFeedUrl(feed.self().location().toString());

        return feedsRecord;
    }

    @SuppressWarnings("unused")
    default Instant fromLocalDateTime(LocalDateTime date) {
        return DateUtils.toInstant(date);
    }
}
