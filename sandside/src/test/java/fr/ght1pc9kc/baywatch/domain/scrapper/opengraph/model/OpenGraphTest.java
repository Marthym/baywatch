package fr.ght1pc9kc.baywatch.domain.scrapper.opengraph.model;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.List;
import java.util.Locale;

public class OpenGraphTest {
    @Test
    void should_build_from_metas() throws MalformedURLException {
        OpenGraph actual = OpenGraph.fromMetas(List.of(
                new Meta("twiter:title", "Title de twiter"),
                new Meta("og:title", "Title de OG"),
                new Meta("og:image", "https://blog.ght1pc9kc.fr/img/featured.jpg"),
                new Meta("og:type", "article"),
                new Meta("og:url", "https://blog.ght1pc9kc.fr/img/over-the-top.html"),
                new Meta("og:description", "Description de OG"),
                new Meta("og:locale", "fr-fr")
        ));

        Assertions.assertThat(actual).isEqualTo(
                OpenGraph.builder()
                        .title("Title de OG")
                        .image(URI.create("https://blog.ght1pc9kc.fr/img/featured.jpg"))
                        .type(OGType.ARTICLE)
                        .url(new URL("https://blog.ght1pc9kc.fr/img/over-the-top.html"))
                        .description("Description de OG")
                        .locale(Locale.FRANCE)
                        .build());
    }

    @ParameterizedTest
    @CsvSource({
            "og:image, none",
            "og:url, bad url",
            "og:locale, bad locale"
    })
    void should_build_with_wrong_metas(String tag, String value) {
        OpenGraph actual = OpenGraph.fromMetas(List.of(new Meta(tag, value)));
        Assertions.assertThat(actual).isNotNull();
    }

    @Test
    void should_build_with_null_locale() {
        OpenGraph actual = OpenGraph.fromMetas(List.of(new Meta("og:locale", null)));
        Assertions.assertThat(actual).isNotNull();
    }
}
