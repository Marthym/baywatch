package fr.ght1pc9kc.baywatch.infra.mappers;

import fr.ght1pc9kc.baywatch.api.model.News;
import fr.ght1pc9kc.baywatch.domain.utils.Hasher;
import fr.ght1pc9kc.baywatch.dsl.tables.records.NewsFeedsRecord;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

import static fr.ght1pc9kc.baywatch.dsl.tables.NewsFeeds.NEWS_FEEDS;

@Component
public class NewsFeedsToRecordConverter implements Converter<News, NewsFeedsRecord> {
    @Override
    public NewsFeedsRecord convert(News source) {
        return NEWS_FEEDS.newRecord()
                .setNefeNewsId(Hasher.sha3(source.link.toString()))
                .setNefeFeedId(source.getFeedId());
    }
}
