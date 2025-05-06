package fr.ght1pc9kc.baywatch.scraper.domain.filters;

import fr.ght1pc9kc.baywatch.techwatch.api.model.RawNews;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import reactor.test.StepVerifier;

import java.net.URI;

class SanitizerFilterTest {
    private static final String LOREM_IPSUM = """
            Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et
            dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex
            ea commodo consequat. Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu
            fugiat nulla pariatur. Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt
            mollit anim id est laborum.
            """;
    private static final String LOREM_H1 = "<h1>Illegal H1 usage</h1>";
    private static final String LOREM_A = "<a href=\"http://www.jedi.com/\">Illegal A usage</a>";

    SanitizerFilter tested = new SanitizerFilter();

    @Test
    void should_sanitize_news_title() {
        RawNews raw = RawNews.builder().id("0").link(URI.create("https://www.jedi.com/"))
                .title(LOREM_H1 + LOREM_A + "<b>" + LOREM_IPSUM)
                .build();

        StepVerifier.create(tested.filter(raw))
                .assertNext(actual -> Assertions.assertThat(actual.title()).isEqualTo(
                        "Illegal H1 usageIllegal A usageLorem ipsum dolor sit amet, consectetur adipiscing " +
                                "elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad " +
                                "minim veniam, quis nostrud exercitati"))
                .verifyComplete();
    }

    @Test
    void should_sanitize_news_title_with_encoded_characters() {
        RawNews raw = RawNews.builder().id("0").link(URI.create("https://www.jedi.com/"))
                .title("Ubuntu va adopter &#34;sudo&#34; en Rust")
                .build();

        StepVerifier.create(tested.filter(raw))
                .assertNext(actual -> Assertions.assertThat(actual.title()).isEqualTo(
                        "Ubuntu va adopter \"sudo\" en Rust"))
                .verifyComplete();
    }

    @Test
    void should_sanitize_news_description() {
        RawNews raw = RawNews.builder().id("0").link(URI.create("https://www.jedi.com/"))
                .description(LOREM_IPSUM + LOREM_H1 + LOREM_A + "<b>" + LOREM_IPSUM
                        + LOREM_IPSUM + LOREM_IPSUM + LOREM_IPSUM + LOREM_IPSUM + LOREM_IPSUM + LOREM_IPSUM
                        + LOREM_IPSUM + LOREM_IPSUM + LOREM_IPSUM + LOREM_IPSUM + LOREM_IPSUM + LOREM_IPSUM)
                .build();

        StepVerifier.create(tested.filter(raw))
                .assertNext(actual -> Assertions.assertThat(actual.description()).startsWith(
                                "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor " +
                                        "incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis " +
                                        "nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. " +
                                        "Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu " +
                                        "fugiat nulla pariatur. Excepteur sint occaecat cupidatat non proident, sunt in " +
                                        "culpa qui officia deserunt mollit anim id est laborum. " +
                                        "Illegal H1 usageIllegal A usage<b>Lorem ipsum dolor")
                        .endsWith("sunt in culpa qui officia deserunt mollit anim id est laborum. Lorem ipsum dolor sit amet, co</b>"))
                .verifyComplete();
    }

    @Test
    void should_sanitize_description_with_code() {
        RawNews raw = RawNews.builder().id("0").link(URI.create("https://www.jedi.com/"))
                .description("""
                        Necesito ayuda con un código HTML, no logro hacer que el .sidebar quede a la izquierda del
                        container verde como en la imagen adjunta.
                        &amp;lt;code&amp;gt;&amp;lt;!DOCTYPE html&amp;gt; &amp;lt;html lang=&amp;quot;es&amp;quot;&amp;gt;
                        &amp;lt;head&amp;gt; &amp;lt;meta charset=&amp;quot;UTF-8&amp;quot;&amp;gt; &amp;lt;title&amp;gt;
                        Recreación de Página&amp;lt;/title&amp;gt; &amp;lt;style&amp;gt; body { margin: 0;
                        font-family: Arial, sans-serif; background-color: #d3d3d3; padding-top: 4%; padding-left: 10%;
                        padding-bottom: 10%; padding-right: 10%; } .navbar { background-color: #444; color: white;
                        display: flex; justify-content: space-around; padding: 15px 0; } .navbar a { color: white;
                        text-decoration: none; padding: 8px 15px; } .sidebar { position: fixed; width: 5%; height: 30%;
                        background-color: #ccc; display: flex; flex-direction: column; align-items: center;
                        padding-top: 1%; border-top-right-radius: 5%; border-bottom-right-radius: 5%; borde&amp;lt;/code&amp;gt;
                        """)
                .build();

        StepVerifier.create(tested.filter(raw))
                .assertNext(actual -> Assertions.assertThat(actual.description()).startsWith(
                        "Necesito ayuda con un código HTML, no logro hacer que el .sidebar quede a la izquierda del " +
                                "container verde como en la imagen adjunta. &lt;code&gt;&lt;!DOCTYPE html&gt;"))
                .verifyComplete();
    }
}