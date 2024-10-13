package fr.ght1pc9kc.baywatch.common.infra.mappers;

import fr.ght1pc9kc.baywatch.common.domain.DateUtils;
import fr.ght1pc9kc.baywatch.common.domain.Hasher;
import fr.ght1pc9kc.baywatch.dsl.tables.records.NewsRecord;
import fr.ght1pc9kc.baywatch.techwatch.api.model.News;
import fr.ght1pc9kc.baywatch.techwatch.api.model.RawNews;
import fr.ght1pc9kc.baywatch.techwatch.api.model.State;
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
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static fr.ght1pc9kc.baywatch.dsl.tables.News.NEWS;
import static fr.ght1pc9kc.baywatch.dsl.tables.NewsFeeds.NEWS_FEEDS;
import static fr.ght1pc9kc.baywatch.dsl.tables.NewsUserState.NEWS_USER_STATE;

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
        RawNews raw = recordToRawNews(r);
        State state = (r.indexOf(NEWS_USER_STATE.NURS_STATE) >= 0)
                ? State.of(r.get(NEWS_USER_STATE.NURS_STATE))
                : State.NONE;
        Set<String> feeds = (r.indexOf(NEWS_FEEDS.NEFE_FEED_ID.getName()) >= 0)
                ? Optional.ofNullable(r.get(NEWS_FEEDS.NEFE_FEED_ID.getName(), String.class))
                .map(s -> Stream.of(s.split(",")).collect(Collectors.toUnmodifiableSet()))
                .orElse(Set.of())
                : Set.of();

        return News.builder()
                .raw(raw)
                .state(state)
                .feeds(feeds)
                .build();
    }

    @SuppressWarnings("unused")
    default Instant fromLocalDateTime(LocalDateTime date) {
        return DateUtils.toInstant(date);
    }
}
