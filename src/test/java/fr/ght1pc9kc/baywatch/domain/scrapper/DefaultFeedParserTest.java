package fr.ght1pc9kc.baywatch.domain.scrapper;

import fr.ght1pc9kc.baywatch.api.model.News;
import fr.ght1pc9kc.baywatch.domain.scrapper.plugins.DefaultParserPlugin;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.List;
import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;

class DefaultFeedParserTest {

    RssAtomParserImpl tested = new RssAtomParserImpl(List.of(new DefaultParserPlugin()));

    @ParameterizedTest
    @CsvSource({
//            "feeds/journal_du_hacker.xml, 25",
            "feeds/reddit-java.xml, 7",
//            "feeds/reddit-prog.xml, 25",
//            "feeds/sebosss.xml, 20",
//            "feeds/spring-blog.xml, 20",
    })
    void should_parse_default_feed(String feedFile, int expectedFeedCount) throws IOException {
        try (InputStream is = DefaultFeedParserTest.class.getResourceAsStream(feedFile)) {
            assertThat(is).isNotNull();

            List<News> actuals = tested.parse(is).collectList().block();

            assertThat(actuals).hasSize(expectedFeedCount);

            News actual = Objects.requireNonNull(actuals).get(0);
            assertThat(actual).isNotNull();
            assertThat(actual.id).isNotNull();
            assertThat(actual.description).isEqualTo("");
            assertThat(actual.link).isEqualTo(URI.create("http://blog.ght1pc9kc.fr/"));
        }
    }
}