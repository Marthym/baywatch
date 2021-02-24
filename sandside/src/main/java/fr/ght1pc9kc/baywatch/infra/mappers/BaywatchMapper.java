package fr.ght1pc9kc.baywatch.infra.mappers;

import fr.ght1pc9kc.baywatch.api.model.*;
import fr.ght1pc9kc.baywatch.domain.utils.Hasher;
import fr.ght1pc9kc.baywatch.dsl.tables.records.*;
import org.jooq.Record;
import org.jooq.tools.StringUtils;
import org.mapstruct.InheritInverseConfiguration;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.net.URI;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Set;

import static fr.ght1pc9kc.baywatch.dsl.tables.Feeds.FEEDS;
import static fr.ght1pc9kc.baywatch.dsl.tables.FeedsUsers.FEEDS_USERS;
import static fr.ght1pc9kc.baywatch.dsl.tables.News.NEWS;
import static fr.ght1pc9kc.baywatch.dsl.tables.NewsFeeds.NEWS_FEEDS;
import static fr.ght1pc9kc.baywatch.dsl.tables.NewsUserState.NEWS_USER_STATE;

@Mapper(componentModel = "spring", imports = {
        Hasher.class, StringUtils.class, Optional.class, URI.class
})
public interface BaywatchMapper {

    @Mapping(source = "id", target = "userId")
    @Mapping(source = "login", target = "userLogin")
    @Mapping(source = "name", target = "userName")
    @Mapping(source = "mail", target = "userEmail")
    @Mapping(source = "password", target = "userPassword")
    UsersRecord userToRecord(User user);

    @InheritInverseConfiguration
    User recordToUser(UsersRecord record);

    default LocalDateTime map(Instant value) {
        return DateUtils.toLocalDateTime(value);
    }

    default String uriToString(URI uri) {
        return uri.toString();
    }

    @Mapping(target = "newsId",
            expression = "java((news.getId() == null) ? Hasher.sha3(news.getLink().toString()) : news.getId())")
    @Mapping(target = "newsTitle", expression = "java( StringUtils.abbreviate(news.getTitle(), 250) )")
    @Mapping(target = "newsDescription", source = "raw.description")
    @Mapping(target = "newsImgLink", expression = "java( Optional.ofNullable(news.getImage()).map(URI::toString).orElse(null) )")
    @Mapping(target = "newsLink", source = "raw.link")
    @Mapping(target = "newsPublication", source = "raw.publication")
    NewsRecord newsToNewsRecord(News news);

    @Mapping(target = "nefeNewsId",
            expression = "java((news.getId() == null) ? Hasher.sha3(news.getLink().toString()) : news.getId())")
    @Mapping(target = "nefeFeedId", source = "feedId")
    NewsFeedsRecord newsToNewsFeedsRecord(News news);

    default RawNews recordToRawNews(Record record) {
        return RawNews.builder()
                .id(record.get(NEWS.NEWS_ID))
                .title(record.get(NEWS.NEWS_TITLE))
                .image(Optional.ofNullable(record.get(NEWS.NEWS_IMG_LINK)).map(URI::create).orElse(null))
                .description(record.get(NEWS.NEWS_DESCRIPTION))
                .link(URI.create(record.get(NEWS.NEWS_LINK)))
                .publication(DateUtils.toInstant(record.get(NEWS.NEWS_PUBLICATION)))
                .build();
    }

    default News recordToNews(Record record) {
        RawNews raw = recordToRawNews(record);
        State state = State.of(record.get(NEWS_USER_STATE.NURS_STATE));
        return News.builder()
                .raw(raw)
                .state(state)
                .feedId(record.get(NEWS_FEEDS.NEFE_FEED_ID))
                .build();
    }

    User principalToUser(Object principal);

    default RawFeed recordToRawFeed(Record record) {
        return RawFeed.builder()
                .id(record.get(FEEDS.FEED_ID))
                .url(URI.create(record.get(FEEDS.FEED_URL)))
                .name(record.get(FEEDS.FEED_NAME))
                .lastWatch(DateUtils.toInstant(record.get(FEEDS.FEED_LAST_WATCH)))
                .build();
    }

    default Feed recordToFeed(Record record) {
        RawFeed raw = recordToRawFeed(record);
        Set<String> tags = Optional.ofNullable(record.get(FEEDS_USERS.FEUS_TAGS))
                .map(t -> Set.of(t.split(",")))
                .orElse(Set.of());
        return Feed.builder()
                .raw(raw)
                .tags(tags)
                .build();
    }

    @Mapping(target = "feedId", source = "raw.id")
    @Mapping(target = "feedUrl", source = "raw.url")
    @Mapping(target = "feedName", source = "raw.name")
    @Mapping(target = "feedLastWatch", source = "raw.lastWatch")
    FeedsRecord feedToFeedsRecord(Feed feed);

    @Mapping(target = "feusFeedId", source = "raw.id")
    @Mapping(target = "feusTags", expression = "java( String.join(\",\", feed.getTags()) )")
    FeedsUsersRecord feedToFeedsUsersRecord(Feed feed);
}