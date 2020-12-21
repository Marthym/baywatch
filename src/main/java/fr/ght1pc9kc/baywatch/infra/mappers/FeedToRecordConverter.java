package fr.ght1pc9kc.baywatch.infra.mappers;

import fr.ght1pc9kc.baywatch.api.model.Feed;
import fr.ght1pc9kc.baywatch.dsl.tables.records.FeedsRecord;
import org.springframework.core.convert.converter.Converter;

import static fr.ght1pc9kc.baywatch.dsl.tables.Feeds.FEEDS;

public class FeedToRecordConverter implements Converter<Feed, FeedsRecord> {
    @Override
    public FeedsRecord convert(Feed source) {
        return FEEDS.newRecord()
                .setFeedId(source.getId())
                .setFeedUrl(source.getUrl().toString())
                .setFeedName(source.getName())
                .setFeedLastWatch(DateUtils.toLocalDateTime(source.getLastWatch()));
    }
}
