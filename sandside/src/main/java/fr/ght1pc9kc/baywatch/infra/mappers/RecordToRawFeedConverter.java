package fr.ght1pc9kc.baywatch.infra.mappers;

import fr.ght1pc9kc.baywatch.api.model.RawFeed;
import org.jooq.Record;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

import java.net.URI;

import static fr.ght1pc9kc.baywatch.dsl.tables.Feeds.FEEDS;

@Component
public class RecordToRawFeedConverter implements Converter<Record, RawFeed> {
    @Override
    public RawFeed convert(Record source) {
        return RawFeed.builder()
                .id(source.get(FEEDS.FEED_ID))
                .url(URI.create(source.get(FEEDS.FEED_URL)))
                .name(source.get(FEEDS.FEED_NAME))
                .lastWatch(DateUtils.toInstant(source.get(FEEDS.FEED_LAST_WATCH)))
                .build();
    }
}
