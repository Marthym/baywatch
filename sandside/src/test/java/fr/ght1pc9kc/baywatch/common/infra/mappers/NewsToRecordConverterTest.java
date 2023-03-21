package fr.ght1pc9kc.baywatch.common.infra.mappers;

import fr.ght1pc9kc.baywatch.common.domain.DateUtils;
import fr.ght1pc9kc.baywatch.common.domain.Hasher;
import fr.ght1pc9kc.baywatch.dsl.tables.records.NewsRecord;
import fr.ght1pc9kc.baywatch.techwatch.api.model.News;
import fr.ght1pc9kc.baywatch.techwatch.api.model.RawNews;
import fr.ght1pc9kc.baywatch.techwatch.api.model.State;
import org.jooq.Record9;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.net.URI;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Set;

import static fr.ght1pc9kc.baywatch.dsl.tables.FeedsUsers.FEEDS_USERS;
import static fr.ght1pc9kc.baywatch.dsl.tables.News.NEWS;
import static fr.ght1pc9kc.baywatch.dsl.tables.NewsFeeds.NEWS_FEEDS;
import static fr.ght1pc9kc.baywatch.dsl.tables.NewsUserState.NEWS_USER_STATE;
import static org.assertj.core.api.Assertions.assertThat;

class NewsToRecordConverterTest {
    private static final Instant PUBLICATION = Instant.parse("2020-11-30T20:09:42Z");
    final BaywatchMapper tested = Mappers.getMapper(BaywatchMapper.class);

    @Test
    void should_convert_News_pojo_to_NewsRecord_with_id() {
        URI link = URI.create("https://blog.ght1pc9kc.fr/index.xml");
        String uuid = Hasher.identify(link);
        NewsRecord actual = tested.newsToNewsRecord(News.builder()
                .raw(RawNews.builder()
                        .id(uuid)
                        .title("Obiwan")
                        .description("Kenobi")
                        .link(link)
                        .publication(PUBLICATION)
                        .build())
                .state(State.NONE)
                .build());

        assertThat(actual).isEqualTo(NEWS.newRecord()
                .setNewsId(uuid)
                .setNewsTitle("Obiwan")
                .setNewsDescription("Kenobi")
                .setNewsLink(link.toString())
                .setNewsPublication(DateUtils.toLocalDateTime(PUBLICATION))
        );
    }

    @Test
    void should_convert_record_to_news() {
        URI link = URI.create("https://blog.ght1pc9kc.fr/index.xml");
        String uuid = Hasher.identify(link);
        Record9<String, String, String, String, String, LocalDateTime, Integer, String, String> r = DSL.using(SQLDialect.H2).newRecord(
                NEWS.NEWS_ID,
                NEWS.NEWS_TITLE,
                NEWS.NEWS_IMG_LINK,
                NEWS.NEWS_DESCRIPTION,
                NEWS.NEWS_LINK,
                NEWS.NEWS_PUBLICATION,
                NEWS_USER_STATE.NURS_STATE,
                NEWS_FEEDS.NEFE_FEED_ID,
                FEEDS_USERS.FEUS_TAGS
        );
        r.set(NEWS.NEWS_ID, uuid);
        r.set(NEWS.NEWS_TITLE, "Obiwan");
        r.set(NEWS.NEWS_DESCRIPTION, "Kenobi");
        r.set(NEWS.NEWS_LINK, link.toString());
        r.set(NEWS.NEWS_PUBLICATION, DateUtils.toLocalDateTime(PUBLICATION));
        r.set(NEWS_FEEDS.NEFE_FEED_ID, "42,42,42,24");
        r.set(FEEDS_USERS.FEUS_TAGS, "test,ou,pas,ou");

        News actual = tested.recordToNews(r);

        assertThat(actual).isEqualTo(News.builder()
                .raw(RawNews.builder()
                        .id(uuid)
                        .title("Obiwan")
                        .description("Kenobi")
                        .link(link)
                        .publication(PUBLICATION)
                        .build())
                .state(State.NONE)
                .feeds(Set.of("42", "24"))
                .tags(Set.of("pas", "ou", "test"))
                .build());
    }
}