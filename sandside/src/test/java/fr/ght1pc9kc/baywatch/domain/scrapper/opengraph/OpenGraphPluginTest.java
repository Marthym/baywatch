package fr.ght1pc9kc.baywatch.domain.scrapper.opengraph;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.util.List;

import static org.mockito.Mockito.*;

public class OpenGraphPluginTest {

    private final OpenGraphPlugin plugin = mock(OpenGraphPlugin.class);

    private OpenGraphScrapper tested;

    @BeforeEach
    void setUp() {
        when(plugin.isApplicable(any())).thenReturn(true);
        WebClient webClient = WebClient.builder().exchangeFunction(request ->
                Mono.just(ClientResponse.create(HttpStatus.OK)
                        .header("content-type", "application/json")
                        .build())).build();
        this.tested = new OpenGraphScrapper(webClient, new OpenGraphMetaReader(), List.of(plugin));
    }

    @Test
    void should_use_plugin_for_scrapper() {
        tested.scrap(URI.create("https://www.youtube.com/watch?v=l9nh1l8ZIJQ")).block();

        verify(plugin, times(1)).additionalHeaders();
        verify(plugin, times(1)).additionalCookies();
    }
}
