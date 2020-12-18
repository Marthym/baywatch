package fr.ght1pc9kc.baywatch.infra.mappers;

import fr.ght1pc9kc.baywatch.api.model.News;
import fr.ght1pc9kc.baywatch.api.model.RawNews;
import fr.ght1pc9kc.baywatch.api.model.State;
import lombok.AllArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.jooq.Record;
import org.springframework.core.convert.ConversionService;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

import static fr.ght1pc9kc.baywatch.dsl.tables.NewsFeeds.NEWS_FEEDS;
import static fr.ght1pc9kc.baywatch.dsl.tables.NewsUserState.NEWS_USER_STATE;

@Component
@AllArgsConstructor
public class RecordToNewsConverter implements Converter<Record, News> {

    private final ConversionService conversionService;

    @Override
    public News convert(@NotNull Record source) {
        RawNews raw = conversionService.convert(source, RawNews.class);
        State state = State.of(source.get(NEWS_USER_STATE.NURS_STATE));
        return News.builder()
                .raw(raw)
                .state(state)
                .feedId(source.get(NEWS_FEEDS.NEFE_FEED_ID))
                .build();
    }

}
