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
    public static final Map<String, Field<?>> NEWS_PROPERTIES_MAPPING = Map.of(
            "publication", NEWS.NEWS_PUBLICATION,
            "description", NEWS.NEWS_DESCRIPTION,
            "id", NEWS.NEWS_ID,
            "link", NEWS.NEWS_LINK,
            "title", NEWS.NEWS_TITLE
    );

    public static final Map<String, Field<?>> STATE_PROPERTIES_MAPPING = Map.of(
            "newsId", NEWS_USER_STATE.NURS_NEWS_ID,
            "useIdr", NEWS_USER_STATE.NURS_USER_ID,
            "state", NEWS_USER_STATE.NURS_STATE
    );

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
