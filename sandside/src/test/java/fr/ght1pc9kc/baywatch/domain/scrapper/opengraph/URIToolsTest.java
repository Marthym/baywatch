package fr.ght1pc9kc.baywatch.domain.scrapper.opengraph;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.net.URI;

class URIToolsTest {
    @ParameterizedTest
    @CsvSource({
            "https://www.jedi.com/obiwan/?q=jedi, https://www.jedi.com/obiwan/",
            "https://www.jedi.com/obiwan?q=jedi, https://www.jedi.com/obiwan",
            "https://www.jedi.com/?obiwan/?q=jedi, https://www.jedi.com/",
    })
    void should_remove_querystring_from_uri(String uri, String expected) {
        URI actual = URITools.removeQueryString(URI.create(uri));
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
        URI actual = URITools.removeQueryString(uri);
        Assertions.assertThat(actual.toString()).isEqualTo(expected);
    }
}