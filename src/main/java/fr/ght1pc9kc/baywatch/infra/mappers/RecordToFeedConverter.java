package fr.ght1pc9kc.baywatch.infra.mappers;


import com.machinezoo.noexception.Exceptions;
import fr.ght1pc9kc.baywatch.dsl.tables.records.FeedsRecord;
import fr.ght1pc9kc.baywatch.api.model.Feed;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

import java.net.URL;
import java.time.Instant;
import java.util.Collections;
import java.util.Optional;

@Component
public class RecordToFeedConverter implements Converter<FeedsRecord, Feed> {
    @Override
    public Feed convert(FeedsRecord feedsRecord) {
        URL url = Exceptions.wrap().get(() -> new URL(feedsRecord.getFeedUrl()));
        return new Feed(
                feedsRecord.getFeedId(),
                feedsRecord.getFeedName(),
                url,
                Optional.ofNullable(feedsRecord.getFeedLastWatch()).map(Instant::parse).orElse(Instant.EPOCH),
                Collections.emptyList()
        );
    }
}
