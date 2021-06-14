package fr.ght1pc9kc.baywatch.domain.scrapper.opengraph.plugins;

import fr.ght1pc9kc.baywatch.domain.scrapper.opengraph.OpenGraphMetaReader;
import fr.ght1pc9kc.baywatch.domain.scrapper.opengraph.OpenGraphPluginTest;
import fr.ght1pc9kc.baywatch.domain.scrapper.opengraph.OpenGraphScrapper;
import fr.ght1pc9kc.baywatch.domain.scrapper.opengraph.model.OGType;
import fr.ght1pc9kc.baywatch.domain.scrapper.opengraph.model.OpenGraph;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.core.io.buffer.DefaultDataBufferFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.InputStream;
import java.net.URI;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.Mockito.spy;

class YoutubeRequestPluginTest {
    private final OpenGraphMetaReader ogReader = spy(new OpenGraphMetaReader());

    private OpenGraphScrapper tested;

    @BeforeEach
    void setUp() {
        InputStream is = OpenGraphPluginTest.class.getResourceAsStream("youtube.html");
        Assertions.assertThat(is).describedAs("youtube.html file must exists !").isNotNull();

        Flux<DataBuffer> data = DataBufferUtils.readInputStream(() -> is, new DefaultDataBufferFactory(), 1024);
        WebClient webClient = WebClient.builder().exchangeFunction(request -> {
            if (request.headers().containsKey("Cookie")) {
                return Mono.just(ClientResponse.create(HttpStatus.OK)
                        .header("content-type", "application/json")
                        .body(data)
                        .build());
            } else {
                return Mono.just(ClientResponse.create(HttpStatus.BAD_REQUEST).build());
            }
        }).build();
        this.tested = new OpenGraphScrapper(webClient, ogReader, List.of(new YoutubeRequestPlugin()));
    }

    @Test
    void should_use_plugin_for_scrapper() {
        OpenGraph actual = tested.scrap(URI.create("https://www.youtube.com/watch?v=l9nh1l8ZIJQ")).block();

        Assertions.assertThat(actual).isNotNull();
        assertAll(
                () -> Assertions.assertThat(actual.image.toString()).isEqualTo("https://i.ytimg.com/vi/l9nh1l8ZIJQ/maxresdefault.jpg"),
                () -> Assertions.assertThat(actual.description).isEqualTo("Stay with Jim ^-^ " +
                        "Enjoy and do not forget to say thank you!Support on Patreon will motivate me more. " +
                        "I need to know that you guys need this stuff and you ape..."),
                () -> Assertions.assertThat(actual.title).isEqualTo("Programming / Coding / Hacking music vol.16 (CONNECTION LOST)"),
                () -> Assertions.assertThat(actual.type).isEqualTo(OGType.VIDEO_OTHER)
        );
    }
}