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
}