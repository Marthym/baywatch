package fr.ght1pc9kc.baywatch.domain.scrapper;

import fr.ght1pc9kc.baywatch.api.model.News;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class DefaultFeedParserTest {

    DefaultFeedParser tested = new DefaultFeedParser();

    @ParameterizedTest
    @CsvSource({
            "feeds/journal_du_hacker.xml, 25",
            "feeds/reddit-java.xml, 7",
            "feeds/sebosss.xml, 20",
            "feeds/spring-blog.xml, 20",
    })
    void should_parse_default_feed(String feedFile, int expectedFeedCount) throws IOException {
        try (InputStream is = DefaultFeedParserTest.class.getResourceAsStream(feedFile)) {
            assertThat(is).isNotNull();

            List<News> actual = tested.parse(is).collectList().block();

            assertThat(actual).hasSize(expectedFeedCount);
        }
    }
}