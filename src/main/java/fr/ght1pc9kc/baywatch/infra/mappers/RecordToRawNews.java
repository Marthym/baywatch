package fr.ght1pc9kc.baywatch.infra.mappers;

import fr.ght1pc9kc.baywatch.api.model.RawNews;
import org.jooq.Record;
import org.springframework.core.convert.converter.Converter;

import java.net.URI;

import static fr.ght1pc9kc.baywatch.dsl.tables.News.NEWS;

public class RecordToRawNews implements Converter<Record, RawNews> {
    @Override
    public RawNews convert(Record source) {
        return RawNews.builder()
                .id(source.get(NEWS.NEWS_ID))
                .title(source.get(NEWS.NEWS_TITLE))
                .link(URI.create(source.get(NEWS.NEWS_LINK)))
                .publication(DateUtils.toInstant(source.get(NEWS.NEWS_PUBLICATION)))
                .build();
    }
}
