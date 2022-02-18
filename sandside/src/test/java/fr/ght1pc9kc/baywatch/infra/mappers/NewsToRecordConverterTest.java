package fr.ght1pc9kc.baywatch.infra.mappers;

import fr.ght1pc9kc.baywatch.api.techwatch.model.News;
import fr.ght1pc9kc.baywatch.api.techwatch.model.RawNews;
import fr.ght1pc9kc.baywatch.api.techwatch.model.State;
import fr.ght1pc9kc.baywatch.domain.common.Hasher;
import fr.ght1pc9kc.baywatch.dsl.tables.records.NewsRecord;
import fr.ght1pc9kc.baywatch.infra.common.mappers.BaywatchMapper;
import fr.ght1pc9kc.baywatch.domain.common.DateUtils;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.net.URI;
import java.time.Instant;

import static fr.ght1pc9kc.baywatch.dsl.tables.News.NEWS;
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
}