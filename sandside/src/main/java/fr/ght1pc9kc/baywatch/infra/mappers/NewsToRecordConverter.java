package fr.ght1pc9kc.baywatch.infra.mappers;

import fr.ght1pc9kc.baywatch.api.model.News;
import fr.ght1pc9kc.baywatch.domain.utils.Hasher;
import fr.ght1pc9kc.baywatch.dsl.tables.records.NewsRecord;
import org.jooq.Field;
import org.jooq.tools.StringUtils;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

import java.util.Map;

import static fr.ght1pc9kc.baywatch.dsl.tables.News.NEWS;
import static fr.ght1pc9kc.baywatch.dsl.tables.NewsUserState.NEWS_USER_STATE;

@Component
public class NewsToRecordConverter implements Converter<News, NewsRecord> {

    @Override
    public NewsRecord convert(News source) {
        String newsId = (source.getId() == null) ? Hasher.sha3(source.getLink().toString()) : source.getId();
        return NEWS.newRecord()
                .setNewsId(newsId)
                .setNewsDescription(source.getDescription())
                .setNewsLink(source.getLink().toString())
                .setNewsPublication(DateUtils.toLocalDateTime(source.getPublication()))
                .setNewsTitle(StringUtils.abbreviate(source.getTitle(), 250));
    }
}
