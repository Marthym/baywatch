package fr.ght1pc9kc.baywatch.domain.scrapper.plugins;

import fr.ght1pc9kc.baywatch.api.techwatch.model.RawNews;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.net.URI;

import static org.assertj.core.api.Assertions.assertThat;

class RedditParserPluginTest {

    private final RedditParserPlugin tested = new RedditParserPlugin();

    @Test
    void should_parse_description() {
        final String content = "&amp;#32; submitted by &amp;#32; &lt;a href=&quot;https://www.reddit.com/user/gisfromscratch&quot;&gt;\n"
                + "            /u/gisfromscratch &lt;/a&gt; &lt;br/&gt; &lt;span&gt;&lt;a href=&quot;https://medium.com/geospatial-intelligence/geoint-app-proof-of-concept-part-i-ramp-up-once-twice-three-times-a-runtime-612d8ae7e0d6&quot;&gt;[link]&lt;/a&gt;&lt;/span&gt;\n"
                + "            &amp;#32; &lt;span&gt;&lt;a href=&quot;https://www.reddit.com/r/java/comments/k3xydl/the_rampup_using_java_openjdk_for_creating_a/&quot;&gt;[commentaires]&lt;/a&gt;&lt;/span&gt;\n"
                + "        ";

        RawNews actual = tested.handleDescriptionEvent(RawNews.builder().id("42"), content).build();

        assertThat(actual.link).isEqualTo(URI.create(
                "https://medium.com/geospatial-intelligence"
                        + "/geoint-app-proof-of-concept-part-i-ramp-up-once-twice-three-times-a-runtime-612d8ae7e0d6"));
    }

    @Test
    void should_parse_reddit_description_without_protect() {
        final String content = "&#32; submitted by &#32; <a href=\"https://www.reddit.com/user/SSFO\"> /u/SSFO </a> <br/> <span>"
                + "<a href=\"http://peoplesfeelings.com/what-is-peoples-feelings/\">[link]</a></span> &#32; <span>"
                + "<a href=\"https://www.reddit.com/r/programming/comments/k7q28d/brief_article_on_viewing_software_as_a_creative/\">[comments]</a></span>";

        RawNews actual = tested.handleDescriptionEvent(RawNews.builder().id("42"), content).build();

        assertThat(actual.link).isEqualTo(URI.create(
                "http://peoplesfeelings.com/what-is-peoples-feelings/"));
    }

    @Test
    void should_handle_link() {
        final URI obiwanLink = URI.create("https://obiwan.kenobi.jedi");
        final URI lukeLink = URI.create("https://luke.skywalker.jedi");
        {
            RawNews actual = tested.handleLinkEvent(RawNews.builder().id("42"), obiwanLink).build();
            assertThat(actual.link).isEqualTo(obiwanLink);
        }
        {
            RawNews actual = tested.handleLinkEvent(RawNews.builder().id("42").link(lukeLink), obiwanLink).build();
            assertThat(actual.link).isEqualTo(lukeLink);
        }
    }

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