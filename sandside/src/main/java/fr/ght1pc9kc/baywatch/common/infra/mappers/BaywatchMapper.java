package fr.ght1pc9kc.baywatch.common.infra.mappers;

import fr.ght1pc9kc.baywatch.common.api.model.Entity;
import fr.ght1pc9kc.baywatch.common.domain.DateUtils;
import fr.ght1pc9kc.baywatch.common.domain.Hasher;
import fr.ght1pc9kc.baywatch.dsl.tables.records.FeedsRecord;
import fr.ght1pc9kc.baywatch.dsl.tables.records.FeedsUsersRecord;
import fr.ght1pc9kc.baywatch.dsl.tables.records.NewsRecord;
import fr.ght1pc9kc.baywatch.dsl.tables.records.UsersRecord;
import fr.ght1pc9kc.baywatch.security.api.model.User;
import fr.ght1pc9kc.baywatch.techwatch.api.model.Feed;
import fr.ght1pc9kc.baywatch.techwatch.api.model.News;
import fr.ght1pc9kc.baywatch.techwatch.api.model.RawFeed;
import fr.ght1pc9kc.baywatch.techwatch.api.model.RawNews;
import fr.ght1pc9kc.baywatch.techwatch.api.model.State;
import org.jooq.Record;
import org.jooq.tools.StringUtils;
import org.mapstruct.InheritInverseConfiguration;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValueCheckStrategy;
import org.mapstruct.NullValuePropertyMappingStrategy;

import java.net.URI;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static fr.ght1pc9kc.baywatch.dsl.tables.Feeds.FEEDS;
import static fr.ght1pc9kc.baywatch.dsl.tables.FeedsUsers.FEEDS_USERS;
import static fr.ght1pc9kc.baywatch.dsl.tables.News.NEWS;
import static fr.ght1pc9kc.baywatch.dsl.tables.NewsFeeds.NEWS_FEEDS;
import static fr.ght1pc9kc.baywatch.dsl.tables.NewsUserState.NEWS_USER_STATE;
import static java.util.function.Predicate.not;

@Mapper(componentModel = "spring", imports = {
        Hasher.class, StringUtils.class, Optional.class, URI.class
})
public interface BaywatchMapper {

    @Mapping(source = "id", target = "userId")
    @Mapping(source = "createdAt", target = "userCreatedAt")
    @Mapping(source = "self.login", target = "userLogin")
    @Mapping(source = "self.name", target = "userName")
    @Mapping(source = "self.mail", target = "userEmail")
    @Mapping(source = "self.password", target = "userPassword",
            nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS,
            nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(source = "self.role", target = "userRole")
    UsersRecord entityUserToRecord(Entity<User> user);

    @InheritInverseConfiguration
    @Mapping(constant = Entity.NO_ONE, target = "createdBy")
    Entity<User> recordToUserEntity(UsersRecord usersRecord);

    default LocalDateTime map(Instant value) {
        return DateUtils.toLocalDateTime(value);
    }

    default String uriToString(URI uri) {
        return uri.toString();
    }

    @Mapping(target = "newsId",
            expression = "java((news.getId() == null) ? Hasher.identify(news.getLink()) : news.getId())")
    @Mapping(target = "newsTitle", expression = "java( StringUtils.abbreviate(news.getTitle(), 250) )")
    @Mapping(target = "newsDescription", source = "raw.description")
    @Mapping(target = "newsImgLink", expression = "java( Optional.ofNullable(news.getImage()).map(URI::toString).orElse(null) )")
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
        RawNews raw = recordToRawNews(r);
        State state = (r.indexOf(NEWS_USER_STATE.NURS_STATE) >= 0)
                ? State.of(r.get(NEWS_USER_STATE.NURS_STATE))
                : State.NONE;
        Set<String> feeds = (r.indexOf(NEWS_FEEDS.NEFE_FEED_ID.getName()) >= 0)
                ? Optional.of(r.get(NEWS_FEEDS.NEFE_FEED_ID.getName(), String.class).split(","))
                .map(Set::of).orElse(Set.of())
                : Set.of();
        Set<String> tags = (r.indexOf(FEEDS_USERS.FEUS_TAGS.getName()) >= 0)
                ? Optional.ofNullable(r.get(FEEDS_USERS.FEUS_TAGS.getName(), String.class))
                .map(s -> Pattern.compile(",").splitAsStream(s).filter(not(String::isBlank))
                        .collect(Collectors.toUnmodifiableSet())).orElse(null)
                : null;
        return News.builder()
                .raw(raw)
                .state(state)
                .feeds(feeds)
                .tags(tags)
                .build();
    }

    default RawFeed recordToRawFeed(Record r) {
        return RawFeed.builder()
                .id(r.get(FEEDS.FEED_ID))
                .url(URI.create(r.get(FEEDS.FEED_URL)))
                .name(r.get(FEEDS.FEED_NAME))
                .description(r.get(FEEDS.FEED_DESCRIPTION))
                .lastWatch(DateUtils.toInstant(r.get(FEEDS.FEED_LAST_WATCH)))
                .build();
    }

    default Feed recordToFeed(Record r) {
        RawFeed raw = recordToRawFeed(r);
        Set<String> tags = (r.indexOf(FEEDS_USERS.FEUS_TAGS) < 0)
                ? Set.of()
                : Optional.ofNullable(r.get(FEEDS_USERS.FEUS_TAGS))
                .map(t -> Set.of(t.split(",")))
                .orElse(Set.of());
        String name = (r.indexOf(FEEDS_USERS.FEUS_FEED_NAME) >= 0 && r.get(FEEDS_USERS.FEUS_FEED_NAME) != null)
                ? r.get(FEEDS_USERS.FEUS_FEED_NAME) : raw.getName();
        return Feed.builder()
                .raw(raw)
                .name(name)
                .tags(tags)
                .build();
    }

    @Mapping(target = "feedId", source = "raw.id")
    @Mapping(target = "feedUrl", source = "raw.url")
    @Mapping(target = "feedName", source = "raw.name")
    @Mapping(target = "feedDescription", source = "raw.description")
    @Mapping(target = "feedLastWatch", source = "raw.lastWatch")
    FeedsRecord feedToFeedsRecord(Feed feed);

    @Mapping(target = "feusFeedId", source = "raw.id")
    @Mapping(target = "feusFeedName", source = "name")
    @Mapping(target = "feusTags",
            expression = "java( (feed.getTags() != null && !feed.getTags().isEmpty())?String.join(\",\", feed.getTags()):null )")
    FeedsUsersRecord feedToFeedsUsersRecord(Feed feed);

    @SuppressWarnings("unused")
    default Instant fromLocalDateTime(LocalDateTime date) {
        return DateUtils.toInstant(date);
    }
}
