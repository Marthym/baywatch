package fr.ght1pc9kc.baywatch.scrapper.domain.filters;

import fr.ght1pc9kc.baywatch.techwatch.api.model.RawNews;
import org.junit.jupiter.api.Test;

import java.net.URI;

class NewsSanitizerFilterTest {
    private static final String LOREM_IPSUM = """
            Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et
            dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex
            ea commodo consequat. Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu
            fugiat nulla pariatur. Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt
            mollit anim id est laborum.
            """;
    private static final String LOREM_H1 = "<h1>Illagal tag usage</h1>";
    private static final String LOREM_A = "<a href=\"http://www.jedi.com/\">Illagal tag usage</h1>";

    NewsSanitizerFilter tested = new NewsSanitizerFilter();

    @Test
    void should_sanitize_news_title() {
        RawNews.builder().id("0").link(URI.create("https://www.jedi.com/"))
                .title(LOREM_H1+LOREM_A+"<p>"+LOREM_IPSUM);
        tested.filter()
    }
}