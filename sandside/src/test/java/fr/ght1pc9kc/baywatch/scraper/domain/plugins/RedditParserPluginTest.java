package fr.ght1pc9kc.baywatch.scraper.domain.plugins;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.net.URI;

import static org.assertj.core.api.Assertions.assertThat;

class RedditParserPluginTest {

    private final RedditParserPlugin tested = new RedditParserPlugin();

    @ParameterizedTest
    @CsvSource({
            "https://www.reddit.com/r/programming/.rss, https://www.reddit.com/r/programming/.rss?sort=new",
            "https://www.reddit.com/r/programming/.rss/, https://www.reddit.com/r/programming/.rss/?sort=new",
            "https://www.reddit.com/r/programming/.rss?test=yes, https://www.reddit.com/r/programming/.rss?test=yes&sort=new"
    })
    void should_modify_url(String uri, String expected) {
        assertThat(tested.uriModifier(URI.create(uri))).isEqualTo(URI.create(expected));
    }
}