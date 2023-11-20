package fr.ght1pc9kc.baywatch.scraper.domain.filters;

import fr.ght1pc9kc.baywatch.techwatch.api.model.RawNews;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import reactor.test.StepVerifier;

import java.net.URI;

import static org.assertj.core.api.Assertions.assertThat;

class RedditNewsFilterTest {
    private static final RawNews RAW_EXTERNAL = RawNews.builder()
            .id("0")
            .link(URI.create("http://www.reddit.com/u/okenobi"))
            .title("Start with this title")
            .description("&amp;#32; submitted by &amp;#32; &lt;a href=&quot;https://www.reddit.com/user/mohan-thatguy&quot;&gt; " +
                    "/u/mohan-thatguy &lt;/a&gt; &lt;br/&gt; &lt;span&gt;&lt;a " +
                    "href=&quot;https://proxiesapi.com/articles/the-ultimate-jsoup-cheatsheet-in-java&quot;&gt;[link]&lt;/a&gt;&lt;/span&gt; " +
                    "&amp;#32; &lt;span&gt;&lt;a " +
                    "href=&quot;https://www.reddit.com/r/java/comments/17zqw6y/the_complete_jsoup_cheatsheet_in_java/&quot;&gt;" +
                    "[comments]&lt;/a&gt;&lt;/span&gt; ").build();

    private static final RawNews RAW_INTERNAL = RawNews.builder()
            .id("0")
            .link(URI.create("https://www.reddit.com/r/java/comments/17zn7tt/regular_classes_with_canonical_constructor/"))
            .title("Start with this title")
            .description("&lt;!-- SC_OFF --&gt;&lt;div class=&quot;md&quot;&gt;&lt;p&gt;" +
                    "Does it make sense to wish regular classes to have canonical constructor in the future?&lt;/p&gt; " +
                    "&lt;pre&gt;&lt;code&gt;@RestController @HttpExchange public UserController(UserService userService) " +
                    "{ … } &lt;/code&gt;&lt;/pre&gt; &lt;p&gt;It’s shorter. The difference from &lt;code&gt;records&lt;" +
                    "/code&gt;s is no implicit accessors.&lt;/p&gt; &lt;p&gt;Except for multiple syntaxes/doing it in " +
                    "multiple ways, what would be the cons?&lt;/p&gt; &lt;p&gt;What is the probability that it would be " +
                    "considered by our great Java language designers?&lt;/p&gt; &lt;/div&gt;&lt;!-- SC_ON --&gt; &amp;#32; " +
                    "submitted by &amp;#32; &lt;a href=&quot;https://www.reddit.com/user/jvjupiter&quot;&gt; /u/jvjupiter " +
                    "&lt;/a&gt; &lt;br/&gt; &lt;span&gt;&lt;a href=&quot;" +
                    "https://www.reddit.com/r/java/comments/17zn7tt/regular_classes_with_canonical_constructor/&quot;" +
                    "&gt;[link]&lt;/a&gt;&lt;/span&gt; &amp;#32; &lt;span&gt;&lt;a href=&quot;" +
                    "https://www.reddit.com/r/java/comments/17zn7tt/regular_classes_with_canonical_constructor/" +
                    "&quot;&gt;[comments]&lt;/a&gt;&lt;/span&gt;").build();

    private final RedditNewsFilter tested = new RedditNewsFilter("https://www.jide.com/reddit.png");

    @Test
    void should_filter_external_reddit() {
        StepVerifier.create(tested.filter(RAW_EXTERNAL))
                .assertNext(actual -> Assertions.assertAll(
                        () -> assertThat(actual.id()).isEqualTo("0c1d1c395b81ed7ee98f25b2a3ac8b0719a256593eb072f8fa5bd926d2413a1b"),
                        () -> assertThat(actual.title()).isEqualTo(RAW_EXTERNAL.title()),
                        () -> assertThat(actual.description()).isEqualTo(RAW_EXTERNAL.description()),
                        () -> assertThat(actual.image()).isNull(),
                        () -> assertThat(actual.link()).isEqualTo(URI.create("https://proxiesapi.com/articles/the-ultimate-jsoup-cheatsheet-in-java"))
                )).verifyComplete();
    }

    @Test
    void should_filter_internal_reddit() {
        StepVerifier.create(tested.filter(RAW_INTERNAL))
                .assertNext(actual -> Assertions.assertAll(
                        () -> assertThat(actual.id()).isEqualTo(RAW_INTERNAL.id()),
                        () -> assertThat(actual.title()).isEqualTo(RAW_INTERNAL.title()),
                        () -> assertThat(actual.description()).isEqualTo(RAW_INTERNAL.description()),
                        () -> assertThat(actual.image()).hasToString("https://www.jide.com/reddit.png"),
                        () -> assertThat(actual.link()).isEqualTo(RAW_INTERNAL.link())
                )).verifyComplete();
    }

    @Test
    void should_filter_reddit_without_reddit() {
        RawNews rawNewsNotReddit = RAW_EXTERNAL.withLink(URI.create("https://www.jedi.com/not/reddit"));
        StepVerifier.create(tested.filter(rawNewsNotReddit))
                .assertNext(actual -> assertThat(actual).isSameAs(rawNewsNotReddit)).verifyComplete();
    }
}