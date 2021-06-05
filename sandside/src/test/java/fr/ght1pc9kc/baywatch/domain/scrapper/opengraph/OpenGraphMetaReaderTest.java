package fr.ght1pc9kc.baywatch.domain.scrapper.opengraph;

import fr.ght1pc9kc.baywatch.domain.scrapper.opengraph.model.Meta;
import fr.ght1pc9kc.baywatch.domain.scrapper.opengraph.model.OGType;
import fr.ght1pc9kc.baywatch.domain.scrapper.opengraph.model.OpenGraph;
import fr.ght1pc9kc.baywatch.domain.scrapper.opengraph.model.Tags;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class OpenGraphMetaReaderTest {
    private final OpenGraphMetaReader tested = new OpenGraphMetaReader();

    @Test
    void should_build_from_metas() throws MalformedURLException {
        OpenGraph actual = tested.read(List.of(
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
            "http://obiwan.kenobi.jedi/tatooine/featured.jpg",
            "http://obiwan.kenobi.jedi/tatooine/featured.jpg?name=Obiwan",
            "//obiwan.kenobi.jedi/tatooine/featured.jpg",
            "//user:password@obiwan.kenobi.jedi/tatooine/featured.jpg",
            "//obiwan.kenobi.jedi/tatooine/featured.jpg?name=Obiwan",
            "/tatooine/featured.jpg",
            "/tatooine/featured.jpg?name=Obiwan",
            "../tatooine/featured.jpg",
    })
    void should_parse_relative_url(String imageLink) {
        OpenGraph actual = tested.read(Collections.singleton(
                new Meta(Tags.OG_IMAGE, imageLink)
        ), URI.create("http://obiwan.kenobi.jedi/posts/padawan-to-master"));

        Assertions.assertThat(actual.image).isEqualTo(URI.create("http://obiwan.kenobi.jedi/tatooine/featured.jpg"));
    }

    @ParameterizedTest
    @CsvSource({
            "og:image, none",
            "og:url, bad url",
            "og:locale, bad locale"
    })
    void should_build_with_wrong_metas(String tag, String value) {
        OpenGraph actual = tested.read(List.of(new Meta(tag, value)));
        Assertions.assertThat(actual).isNotNull();
    }

    @Test
    void should_build_with_null_locale() {
        OpenGraph actual = tested.read(List.of(new Meta(Tags.OG_LOCALE, null)));
        Assertions.assertThat(actual).isNotNull();
    }

    @Test
    void should_build_with_null_url() {
        OpenGraph actual = tested.read(List.of(new Meta(Tags.OG_URL, null)));
        Assertions.assertThat(actual).isNotNull();
    }
}
