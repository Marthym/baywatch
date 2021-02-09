package fr.ght1pc9kc.baywatch.infra.mappers;

import fr.ght1pc9kc.baywatch.api.model.RawNews;
import org.jooq.Record;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.util.Optional;

import static fr.ght1pc9kc.baywatch.dsl.tables.News.NEWS;

@Component
public class RecordToRawNewsConverter implements Converter<Record, RawNews> {
    @Override
    public RawNews convert(Record source) {
        return RawNews.builder()
                .id(source.get(NEWS.NEWS_ID))
                .title(source.get(NEWS.NEWS_TITLE))
                .image(Optional.ofNullable(source.get(NEWS.NEWS_IMG_LINK)).map(URI::create).orElse(null))
                .description(source.get(NEWS.NEWS_DESCRIPTION))
                .link(URI.create(source.get(NEWS.NEWS_LINK)))
                .publication(DateUtils.toInstant(source.get(NEWS.NEWS_PUBLICATION)))
                .build();
    }
}
