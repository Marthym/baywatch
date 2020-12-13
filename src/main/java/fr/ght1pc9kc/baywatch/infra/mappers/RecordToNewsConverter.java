package fr.ght1pc9kc.baywatch.infra.mappers;

import fr.ght1pc9kc.baywatch.api.model.News;
import org.jooq.Record;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

import java.net.URI;

import static fr.ght1pc9kc.baywatch.dsl.tables.News.NEWS;
import static fr.ght1pc9kc.baywatch.dsl.tables.NewsFeeds.NEWS_FEEDS;

@Component
public class RecordToNewsConverter implements Converter<Record, News> {
    @Override
    public News convert(Record source) {
        return News.builder()
                .id(source.get(NEWS.NEWS_ID))
                .title(source.get(NEWS.NEWS_TITLE))
                .link(URI.create(source.get(NEWS.NEWS_LINK)))
                .publication(DateUtils.toInstant(source.get(NEWS.NEWS_PUBLICATION)))
                .stared(source.get(NEWS.NEWS_STARED))
                .feedId(source.get(NEWS_FEEDS.NEFE_FEED_ID))
                .build();
    }

}
