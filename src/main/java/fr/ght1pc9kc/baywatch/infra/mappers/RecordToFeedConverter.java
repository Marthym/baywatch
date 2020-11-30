package fr.ght1pc9kc.baywatch.infra.mappers;


import fr.ght1pc9kc.baywatch.api.model.Feed;
import fr.ght1pc9kc.baywatch.dsl.tables.records.FeedsRecord;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.util.Optional;

@Component
public class RecordToFeedConverter implements Converter<FeedsRecord, Feed> {
    @Override
    public Feed convert(FeedsRecord feedsRecord) {
        return Feed.builder()
                .id(Optional.ofNullable(feedsRecord.getFeedId()).orElse(0))
                .name(feedsRecord.getFeedName())
                .url(URI.create(feedsRecord.getFeedUrl()))
                .lastWatch(DateUtils.toInstant(feedsRecord.getFeedLastWatch()))
                .build();
    }
}
