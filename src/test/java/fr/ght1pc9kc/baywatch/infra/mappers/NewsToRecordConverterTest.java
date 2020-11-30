package fr.ght1pc9kc.baywatch.infra.mappers;

import fr.ght1pc9kc.baywatch.api.model.News;
import fr.ght1pc9kc.baywatch.dsl.tables.records.NewsRecord;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.time.Instant;

import static fr.ght1pc9kc.baywatch.dsl.tables.News.NEWS;
import static org.assertj.core.api.Assertions.assertThat;

class NewsToRecordConverterTest {
    private static final Instant PUBLICATION = Instant.parse("2020-11-30T20:09:42Z");
    final NewsToRecordConverter tested = new NewsToRecordConverter();

    @Test
    void should_convert_News_pojo_to_NewsRecord() {
        NewsRecord actual = tested.convert(News.builder()
                .id(0)
                .title("Obiwan")
                .description("Kenobi")
                .link(URI.create("https://blog.ght1pc9kc.fr/index.xml"))
                .publication(PUBLICATION)
                .stared(false)
                .build());

        assertThat(actual).isEqualTo(NEWS.newRecord()
                .setNewsTitle("Obiwan")
                .setNewsDescription("Kenobi")
                .setNewsLink(URI.create("https://blog.ght1pc9kc.fr/index.xml").toString())
                .setNewsPublication(DateUtils.toLocalDateTime(PUBLICATION))
                .setNewsStared(false)
        );
    }

    @Test
    void should_convert_News_pojo_to_NewsRecord_with_id() {
        NewsRecord actual = tested.convert(News.builder()
                .id(42)
                .title("Obiwan")
                .description("Kenobi")
                .link(URI.create("https://blog.ght1pc9kc.fr/index.xml"))
                .publication(PUBLICATION)
                .stared(false)
                .build());

        assertThat(actual).isEqualTo(NEWS.newRecord()
                .setNewsFeedId(42)
                .setNewsTitle("Obiwan")
                .setNewsDescription("Kenobi")
                .setNewsLink(URI.create("https://blog.ght1pc9kc.fr/index.xml").toString())
                .setNewsPublication(DateUtils.toLocalDateTime(PUBLICATION))
                .setNewsStared(false)
        );
    }
}