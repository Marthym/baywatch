package fr.ght1pc9kc.baywatch.domain.scrapper;

import fr.ght1pc9kc.baywatch.api.techwatch.model.Feed;
import fr.ght1pc9kc.baywatch.api.techwatch.model.News;
import fr.ght1pc9kc.baywatch.api.techwatch.model.RawFeed;
import fr.ght1pc9kc.baywatch.domain.scrapper.plugins.DefaultParserPlugin;
import fr.ght1pc9kc.baywatch.domain.common.Hasher;
import fr.ght1pc9kc.baywatch.infra.scrapper.config.ScrapperProperties;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.time.*;
import java.util.List;
import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;

class DefaultFeedParserTest {

    private final ScrapperProperties props = new ScrapperProperties(false, Duration.ZERO, Period.ofDays(2));
    private final RssAtomParserImpl tested = new RssAtomParserImpl(props, List.of(new DefaultParserPlugin()));

    @ParameterizedTest(name = "{1}")
    @CsvSource({
            "2020-11-30T12:10:00Z, feeds/journal_du_hacker.xml, 25",
            "2020-11-30T12:10:00Z, feeds/reddit-java.xml, 7",
            "2020-11-30T12:10:00Z, feeds/reddit-prog.xml, 25",
            "2020-11-18T12:10:00Z, feeds/sebosss.xml, 2",
            "2020-11-26T12:10:00Z, feeds/spring-blog.xml, 4",
            "2021-05-20T12:10:00Z, feeds/lemonde.xml, 10",
    })
    void should_parse_default_feed(Instant clock, URI feedFile, int expectedFeedCount) throws IOException {
        tested.setClock(Clock.fixed(clock, ZoneOffset.UTC));
        try (InputStream is = DefaultFeedParserTest.class.getResourceAsStream(feedFile.toString())) {
            assertThat(is).isNotNull();

            Feed feed = Feed.builder()
                    .raw(RawFeed.builder().id(Hasher.identify(feedFile)).url(feedFile).build())
                    .build();
            List<News> actuals = tested.parse(feed, is).collectList().block();

            assertThat(actuals).hasSize(expectedFeedCount);

            News actual = Objects.requireNonNull(actuals).get(0);
            assertThat(actual).isNotNull();
            assertThat(actual.getId()).isNotEmpty().hasSize(64);
            assertThat(actual.getTitle()).isNotEmpty();
            assertThat(actual.getDescription()).isNotEmpty();
            assertThat(actual.getLink()).isNotNull();
        }
    }
}