package fr.ght1pc9kc.baywatch.scrapper.domain.opengraph;

import fr.ght1pc9kc.baywatch.scrapper.domain.opengraph.model.Meta;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.List;
import java.util.stream.Stream;

class OGScrapperUtilsTest {
    @ParameterizedTest
    @CsvSource({
            "https://www.jedi.com/obiwan/?q=jedi, https://www.jedi.com/obiwan/",
            "https://www.jedi.com/obiwan?q=jedi, https://www.jedi.com/obiwan",
            "https://www.jedi.com/?obiwan/?q=jedi, https://www.jedi.com/",
    })
    void should_remove_querystring_from_uri(String uri, String expected) {
        URI actual = OGScrapperUtils.removeQueryString(URI.create(uri));
        Assertions.assertThat(actual.toString()).isEqualTo(expected);
    }

    @ParameterizedTest
    @CsvSource({
            "https://www.jedi.com/obiwan/?q=jedi, https://www.jedi.com/obiwan/",
            "https://www.jedi.com/obiwan?q=jedi, https://www.jedi.com/obiwan",
            "https://www.jedi.com/?obiwan/?q=jedi, https://www.jedi.com/",
            "www.jedi.com/?obiwan/?q=jedi, www.jedi.com/",
    })
    void should_remove_querystring_from_string(String uri, String expected) {
        String actual = OGScrapperUtils.removeQueryString(uri);
        Assertions.assertThat(actual).isEqualTo(expected);
    }

    @ParameterizedTest
    @MethodSource("parametersForShould_extract_meta_headers")
    void should_extract_meta_headers(String htmlFile, List<Meta> expected) throws IOException {
        InputStream is = OpenGraphScrapperTest.class.getResourceAsStream(htmlFile);
        Assertions.assertThat(is).isNotNull();
        List<Meta> actuals = OGScrapperUtils.extractMetaHeaders(new String(is.readAllBytes())).collectList().block();
        Assertions.assertThat(actuals).isNotNull();

        Assertions.assertThat(actuals).containsAll(expected);
    }

    private static Stream<Arguments> parametersForShould_extract_meta_headers() {
        return Stream.of(
                Arguments.of("apostrophe.html", List.of(
                        new Meta("og:title", "Économiseur d'écran personnalisé avec XSecureLock"),
                        new Meta("og:type", "article"),
                        new Meta("og:image", "https://d1g3mdmxf8zbo9.cloudfront.net/images/i3/xsecurelock@2x.jpg"),
                        new Meta("og:locale", "fr_FR"),
                        new Meta("og:description", "XSecureLock permet de verrouiller une session " +
                                "X11 et délègue la partie économiseur d'écran à un programme tiers, permettant " +
                                "de personnaliser...")
                )),
                Arguments.of("ght-bad-parsing.html", List.of(
                        new Meta("og:title", "Les Critères de recherche avec Juery"),
                        new Meta("og:type", "article"),
                        new Meta("og:url", "https://blog.ght1pc9kc.fr/2021/les-crit%C3%A8res-de-recherche-avec-juery.html"),
                        new Meta("og:description", "")
                )),
                Arguments.of("og-head-test.html", List.of(
                        new Meta("og:title", "De Paris à Toulouse"),
                        new Meta("og:type", "article"),
                        new Meta("og:url", "https://blog.i-run.si/posts/silife/infra-de-paris-a-toulouse/"),
                        new Meta("og:image", "https://blog.i-run.si/posts/silife/infra-de-paris-a-toulouse/featured.jpg"),
                        new Meta("og:description", "Déplacement des serveurs de l’infrastructure " +
                                "i-Run depuis Paris jusqu’à Toulouse chez notre hébergeur FullSave. Nouvelles machines, " +
                                "nouvelle infra pour plus de résilience et une meilleure tenue de la charge sur les " +
                                "sites publics comme sur le backoffice.")
                )),
                Arguments.of("youtube.html", List.of(
                        new Meta("og:title", "Programming / Coding / Hacking music vol.16 (CONNECTION LOST)"),
                        new Meta("og:type", "video.other"),
                        new Meta("og:url", "https://www.youtube.com/watch?v=l9nh1l8ZIJQ"),
                        new Meta("og:image", "https://i.ytimg.com/vi/l9nh1l8ZIJQ/maxresdefault.jpg"),
                        new Meta("og:site_name", "YouTube"),
                        new Meta("og:description", "Stay with Jim ^-^ Enjoy and do not forget to say " +
                                "thank you!Support on Patreon will motivate me more. I need to know that you guys " +
                                "need this stuff and you ape...")))
        );
    }
}