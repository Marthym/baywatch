package fr.ght1pc9kc.baywatch.infra.mappers;

import fr.ght1pc9kc.baywatch.api.model.News;
import fr.ght1pc9kc.baywatch.domain.utils.Hasher;
import fr.ght1pc9kc.baywatch.dsl.tables.records.NewsRecord;
import org.jooq.tools.StringUtils;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

import static fr.ght1pc9kc.baywatch.dsl.tables.News.NEWS;

@Component
public class NewsToRecordConverter implements Converter<News, NewsRecord> {
    @Override
    public NewsRecord convert(News source) {
        String newsId = (source.id == null) ? Hasher.sha3(source.link.toString()) : source.id;

        return NEWS.newRecord()
                .setNewsId(newsId)
                .setNewsDescription(source.getDescription())
                .setNewsLink(source.getLink().toString())
                .setNewsPublication(DateUtils.toLocalDateTime(source.getPublication()))
                .setNewsTitle(StringUtils.abbreviate(source.getTitle(), 250))
                .setNewsStared(source.isStared());
    }
}
