package fr.ght1pc9kc.baywatch.common.infra.mappers;

import fr.ght1pc9kc.baywatch.common.domain.DateUtils;
import fr.ght1pc9kc.baywatch.common.domain.Hasher;
import fr.ght1pc9kc.baywatch.dsl.tables.records.FeedsRecord;
import fr.ght1pc9kc.baywatch.dsl.tables.records.FeedsUsersRecord;
import fr.ght1pc9kc.baywatch.dsl.tables.records.NewsRecord;
import fr.ght1pc9kc.baywatch.techwatch.api.model.News;
import fr.ght1pc9kc.baywatch.techwatch.api.model.RawNews;
import fr.ght1pc9kc.baywatch.techwatch.api.model.State;
import fr.ght1pc9kc.baywatch.techwatch.api.model.WebFeed;
import fr.ght1pc9kc.entity.api.Entity;
import org.jooq.Record;
import org.jooq.tools.StringUtils;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import java.net.URI;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static fr.ght1pc9kc.baywatch.common.api.model.FeedMeta.ETag;
import static fr.ght1pc9kc.baywatch.common.api.model.FeedMeta.createdBy;
import static fr.ght1pc9kc.baywatch.common.api.model.FeedMeta.updated;
import static fr.ght1pc9kc.baywatch.dsl.tables.Feeds.FEEDS;
import static fr.ght1pc9kc.baywatch.dsl.tables.FeedsUsers.FEEDS_USERS;
import static fr.ght1pc9kc.baywatch.dsl.tables.News.NEWS;
import static fr.ght1pc9kc.baywatch.dsl.tables.NewsFeeds.NEWS_FEEDS;
import static fr.ght1pc9kc.baywatch.dsl.tables.NewsUserState.NEWS_USER_STATE;
import static java.util.function.Predicate.not;

@Mapper(componentModel = "spring",
        imports = {Hasher.class, StringUtils.class, Optional.class, URI.class},
        unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface BaywatchMapper {

    default LocalDateTime map(Instant value) {
        return DateUtils.toLocalDateTime(value);
    }

    default String uriToString(URI uri) {
        return uri.toString();
    }

    @Mapping(target = "newsId",
            expression = "java((news.id() == null) ? Hasher.identify(news.link()) : news.id())")
    @Mapping(target = "newsTitle", expression = "java( StringUtils.abbreviate(news.title(), 250) )")
    @Mapping(target = "newsDescription", source = "raw.description")
    @Mapping(target = "newsImgLink", expression = "java( Optional.ofNullable(news.image()).map(URI::toString).orElse(null) )")
    @Mapping(target = "newsLink", source = "raw.link")
    @Mapping(target = "newsPublication", source = "raw.publication")
    NewsRecord newsToNewsRecord(News news);

    default RawNews recordToRawNews(Record r) {
        return RawNews.builder()
                .id(r.get(NEWS.NEWS_ID))
                .title(r.get(NEWS.NEWS_TITLE))
                .image(Optional.ofNullable(r.get(NEWS.NEWS_IMG_LINK)).map(URI::create).orElse(null))
                .description(r.get(NEWS.NEWS_DESCRIPTION))
                .link(URI.create(r.get(NEWS.NEWS_LINK)))
                .publication(DateUtils.toInstant(r.get(NEWS.NEWS_PUBLICATION)))
                .build();
    }

    default News recordToNews(Record r) {
        Pattern commaSplitPattern = Pattern.compile(",");
        RawNews raw = recordToRawNews(r);
        State state = (r.indexOf(NEWS_USER_STATE.NURS_STATE) >= 0)
                ? State.of(r.get(NEWS_USER_STATE.NURS_STATE))
                : State.NONE;
        Set<String> feeds = (r.indexOf(NEWS_FEEDS.NEFE_FEED_ID.getName()) >= 0)
                ? Optional.ofNullable(r.get(NEWS_FEEDS.NEFE_FEED_ID.getName(), String.class))
                .map(s -> Stream.of(s.split(",")).collect(Collectors.toUnmodifiableSet()))
                .orElse(Set.of())
                : Set.of();
        Set<String> tags = (r.indexOf(FEEDS_USERS.FEUS_TAGS.getName()) >= 0)
                ? Optional.ofNullable(r.get(FEEDS_USERS.FEUS_TAGS.getName(), String.class))
                .map(s -> commaSplitPattern.splitAsStream(s).filter(not(String::isBlank))
                        .collect(Collectors.toUnmodifiableSet())).orElse(null)
                : null;
        return News.builder()
                .raw(raw)
                .state(state)
                .feeds(feeds)
                .tags(tags)
                .build();
    }

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
