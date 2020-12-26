package fr.ght1pc9kc.baywatch.infra.mappers;


import fr.ght1pc9kc.baywatch.api.model.Feed;
import fr.ght1pc9kc.baywatch.api.model.RawFeed;
import org.jetbrains.annotations.NotNull;
import org.jooq.Record;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.Set;

import static fr.ght1pc9kc.baywatch.dsl.tables.FeedsUsers.FEEDS_USERS;

@Component
public class RecordToFeedConverter implements Converter<Record, Feed> {
    private static final RecordToRawFeedConverter conversionService = new RecordToRawFeedConverter();

    @Override
    public Feed convert(@NotNull Record record) {
        RawFeed raw = conversionService.convert(record);
        Set<String> tags = Optional.ofNullable(record.get(FEEDS_USERS.FEUS_TAGS))
                .map(t -> Set.of(t.split(",")))
                .orElse(Set.of());
        return Feed.builder()
                .raw(raw)
                .tags(tags)
                .build();
    }
}
