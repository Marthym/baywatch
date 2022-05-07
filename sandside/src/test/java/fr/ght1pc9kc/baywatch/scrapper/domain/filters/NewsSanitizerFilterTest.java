package fr.ght1pc9kc.baywatch.scrapper.domain.filters;

import fr.ght1pc9kc.baywatch.techwatch.api.model.RawNews;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import reactor.test.StepVerifier;

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
        RawNews raw = RawNews.builder().id("0").link(URI.create("https://www.jedi.com/"))
                .title(LOREM_H1 + LOREM_A + "<p>" + LOREM_IPSUM)
                .build();

        StepVerifier.create(tested.filter(raw))
                .assertNext(actual -> Assertions.assertThat(actual.getTitle()).isEqualTo(
                        "Illagal tag usageIllagal tag usageLorem ipsum dolor sit amet, consectetur adipiscing " +
                                "elit, sed do eiusmod tempor incididunt ut labore et\n" +
                                "dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exerci"))
                .verifyComplete();
    }

    @Test
    void should_sanitize_news_description() {
        RawNews raw = RawNews.builder().id("0").link(URI.create("https://www.jedi.com/"))
                .description(LOREM_IPSUM + LOREM_H1 + LOREM_A + "<p>" + LOREM_IPSUM
                        + LOREM_IPSUM + LOREM_IPSUM + LOREM_IPSUM + LOREM_IPSUM + LOREM_IPSUM + LOREM_IPSUM
                        + LOREM_IPSUM + LOREM_IPSUM + LOREM_IPSUM + LOREM_IPSUM + LOREM_IPSUM + LOREM_IPSUM)
                .build();

        StepVerifier.create(tested.filter(raw))
                .assertNext(actual -> Assertions.assertThat(actual.getDescription()).startsWith(
                                "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et\n" +
                                        "dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex\n" +
                                        "ea commodo consequat. Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu\n" +
                                        "fugiat nulla pariatur. Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt\n" +
                                        "mollit anim id est laborum.\n" +
                                        "Illagal tag usageIllagal tag usage<p>Lorem ipsum dolor sit amet")
                        .endsWith("quis nostrud exercitation ullamco laboris nisi ut aliquip ex\n" +
                                "ea commodo consequat. Duis aute </p>"))
                .verifyComplete();
    }
}