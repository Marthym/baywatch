package fr.ght1pc9kc.baywatch.mappers;

import fr.ght1pc9kc.baywatch.dsl.tables.records.NewsRecord;
import fr.ght1pc9kc.baywatch.model.News;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

import static fr.ght1pc9kc.baywatch.dsl.tables.News.NEWS;

@Component
public class NewsToRecordConverter implements Converter<News, NewsRecord> {
    @Override
    public NewsRecord convert(News source) {
        NewsRecord newsRecord = NEWS.newRecord();
        newsRecord.setNewsFeedId(source.getId());
        newsRecord.setNewsDescription(source.getDescription());
        newsRecord.setNewsLink(source.getLink().toString());
        newsRecord.setNewsPublication(source.getPublication().toString());
        newsRecord.setNewsTitle(source.getTitle());
        newsRecord.setNewsStared(Boolean.compare(source.isStared(), false));
        return newsRecord;
    }
}
