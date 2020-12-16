package fr.ght1pc9kc.baywatch.infra.mappers;

import fr.ght1pc9kc.baywatch.api.model.News;
import fr.ght1pc9kc.baywatch.api.model.RawNews;
import org.jooq.Record;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.util.Optional;

import static fr.ght1pc9kc.baywatch.dsl.tables.News.NEWS;
import static fr.ght1pc9kc.baywatch.dsl.tables.NewsFeeds.NEWS_FEEDS;
import static fr.ght1pc9kc.baywatch.dsl.tables.NewsUserState.NEWS_USER_STATE;

@Component
public class RecordToNewsConverter implements Converter<Record, News> {
    @Override
    public News convert(Record source) {
        RawNews raw = RawNews.builder()
                .id(source.get(NEWS.NEWS_ID))
                .title(source.get(NEWS.NEWS_TITLE))
                .link(URI.create(source.get(NEWS.NEWS_LINK)))
                .publication(DateUtils.toInstant(source.get(NEWS.NEWS_PUBLICATION)))
                .build();
        return News.builder()
                .raw(raw)
                .state(Optional.ofNullable(source.get(NEWS_USER_STATE.NURS_STATE)).orElse(0))
                .feedId(source.get(NEWS_FEEDS.NEFE_FEED_ID))
                .build();
    }

}
