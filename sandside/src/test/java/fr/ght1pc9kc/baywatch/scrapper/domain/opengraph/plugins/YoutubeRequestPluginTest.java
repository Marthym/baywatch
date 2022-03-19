package fr.ght1pc9kc.baywatch.scrapper.domain.opengraph.plugins;

import fr.ght1pc9kc.scraphead.core.HeadScraper;
import fr.ght1pc9kc.scraphead.core.HeadScrapers;
import fr.ght1pc9kc.scraphead.core.http.WebClient;
import fr.ght1pc9kc.scraphead.core.http.WebRequest;
import fr.ght1pc9kc.scraphead.core.http.WebResponse;
import fr.ght1pc9kc.scraphead.core.model.opengraph.OGType;
import fr.ght1pc9kc.scraphead.core.model.opengraph.OpenGraph;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpHeaders;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class YoutubeRequestPluginTest {
    private HeadScraper tested;

    @BeforeEach
    void setUp() throws IOException {
        Flux<ByteBuffer> data;
        try (InputStream is = YoutubeRequestPluginTest.class.getResourceAsStream("youtube.html")) {
            Assertions.assertThat(is).describedAs("youtube.html file must exists !").isNotNull();

            data = Flux.just(ByteBuffer.wrap(is.readAllBytes()));
        }

        WebClient webClient = mock(WebClient.class);
        when(webClient.send(any(WebRequest.class)))
                .thenAnswer(invocation -> {
                    WebRequest request = invocation.getArgument(0, WebRequest.class);
                    if (request.headers().firstValue("Cookie").isPresent()) {
                        return Mono.just(new WebResponse(
                                200,
                                HttpHeaders.of(Map.of("content-type", List.of("application/json")), (l, r) -> true),
                                data));
                    } else {
                        return Mono.just(new WebResponse(400, null, Flux.empty()));
                    }
                });

        tested = HeadScrapers.builder(webClient).registerPlugin(new YoutubeRequestPlugin()).build();
    }

    @Test
    void should_use_plugin_for_scrapper() {
        OpenGraph actual = tested.scrapOpenGraph(URI.create("https://www.youtube.com/watch?v=l9nh1l8ZIJQ")).block();

        Assertions.assertThat(actual).isNotNull();
        assertAll(
                () -> Assertions.assertThat(actual.image).hasToString("https://i.ytimg.com/vi/l9nh1l8ZIJQ/maxresdefault.jpg"),
                () -> Assertions.assertThat(actual.description).isEqualTo("Stay with Jim ^-^ " +
                        "Enjoy and do not forget to say thank you!Support on Patreon will motivate me more. " +
                        "I need to know that you guys need this stuff and you ape..."),
                () -> Assertions.assertThat(actual.title).isEqualTo("Programming / Coding / Hacking music vol.16 (CONNECTION LOST)"),
                () -> Assertions.assertThat(actual.type).isEqualTo(OGType.VIDEO_OTHER)
        );
    }
}