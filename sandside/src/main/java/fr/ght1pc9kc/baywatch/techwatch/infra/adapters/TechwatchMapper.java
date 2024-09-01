package fr.ght1pc9kc.baywatch.techwatch.infra.adapters;

import fr.ght1pc9kc.baywatch.common.domain.DateUtils;
import fr.ght1pc9kc.baywatch.common.domain.Hasher;
import fr.ght1pc9kc.baywatch.dsl.tables.records.FeedsRecord;
import fr.ght1pc9kc.baywatch.dsl.tables.records.FeedsUsersPropertiesRecord;
import fr.ght1pc9kc.baywatch.dsl.tables.records.FeedsUsersRecord;
import fr.ght1pc9kc.baywatch.techwatch.api.model.WebFeed;
import fr.ght1pc9kc.entity.api.Entity;
import org.jooq.Record;
import org.jooq.UpdatableRecord;
import org.jooq.tools.StringUtils;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

import java.net.URI;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static fr.ght1pc9kc.baywatch.common.api.model.FeedMeta.ETag;
import static fr.ght1pc9kc.baywatch.common.api.model.FeedMeta.createdBy;
import static fr.ght1pc9kc.baywatch.common.api.model.FeedMeta.updated;
import static fr.ght1pc9kc.baywatch.dsl.tables.Feeds.FEEDS;
import static fr.ght1pc9kc.baywatch.dsl.tables.FeedsUsers.FEEDS_USERS;
import static fr.ght1pc9kc.baywatch.dsl.tables.FeedsUsersProperties.FEEDS_USERS_PROPERTIES;
import static fr.ght1pc9kc.baywatch.techwatch.infra.model.FeedProperties.DESCRIPTION;

@Mapper(componentModel = "spring",
        imports = {Hasher.class, StringUtils.class, Optional.class, URI.class},
        unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface TechwatchMapper {
    default Entity<WebFeed> recordToFeed(Record r) {
        Set<String> tags = (r.indexOf(FEEDS_USERS.FEUS_TAGS) < 0)
                ? Set.of()
                : Optional.ofNullable(r.get(FEEDS_USERS.FEUS_TAGS))
                .map(t -> Set.of(t.split(",")))
                .orElse(Set.of());
        String name = (r.indexOf(FEEDS_USERS.FEUS_FEED_NAME) >= 0 && r.get(FEEDS_USERS.FEUS_FEED_NAME) != null)
                ? r.get(FEEDS_USERS.FEUS_FEED_NAME) : r.get(FEEDS.FEED_NAME);

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

    default List<UpdatableRecord<?>> webFeedToRecords(Entity<WebFeed> feed) {
        String userId = feed.meta(createdBy).orElse(null);

        FeedsUsersRecord feedsUsersRecord = FEEDS_USERS.newRecord()
                .setFeusFeedId(feed.id())
                .setFeusUserId(userId);

        if (feed.self().name() != null) {
            feedsUsersRecord.setFeusFeedName(feed.self().name());
        }
        if (!feed.self().tags().isEmpty()) {
            feedsUsersRecord.setFeusTags(String.join(",", feed.self().tags()));
        }

        FeedsUsersPropertiesRecord feedsUsersPropertiesRecord = FEEDS_USERS_PROPERTIES.newRecord()
                .setFuprFeedId(feed.id())
                .setFuprUserId(userId)
                .setFuprPropertyName(DESCRIPTION.name())
                .setFuprPropertyValue(feed.self().description());

        return List.of(feedsUsersRecord, feedsUsersPropertiesRecord);
    }

    default FeedsUsersRecord feedToFeedsUsersRecord(Entity<WebFeed> feed) {
        FeedsUsersRecord feedsUsersRecord = FEEDS_USERS.newRecord();
        feedsUsersRecord.setFeusFeedId(feed.id());
        feed.meta(createdBy).ifPresent(feedsUsersRecord::setFeusUserId);
        if (feed.self().name() != null) {
            feedsUsersRecord.setFeusFeedName(feed.self().name());
        }
        if (!feed.self().tags().isEmpty()) {
            feedsUsersRecord.setFeusTags(String.join(",", feed.self().tags()));
        }
        return feedsUsersRecord;
    }

    @SuppressWarnings("unused")
    default Instant fromLocalDateTime(LocalDateTime date) {
        return DateUtils.toInstant(date);
    }
}
