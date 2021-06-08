package fr.ght1pc9kc.baywatch.domain.scrapper.opengraph;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import fr.ght1pc9kc.baywatch.domain.scrapper.opengraph.model.OpenGraph;
import fr.ght1pc9kc.baywatch.domain.scrapper.opengraph.plugins.YoutubeRequestPlugin;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriBuilder;
import reactor.netty.http.client.HttpClient;
import wiremock.org.apache.http.client.utils.URIBuilder;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.List;
import java.util.Random;

import static org.mockito.Mockito.spy;

public class OpenGraphPluginTest {
    static final WireMockServer mockServer = new WireMockServer(WireMockConfiguration.wireMockConfig().dynamicPort());

    private final OpenGraphMetaReader ogReader = spy(new OpenGraphMetaReader());

    private OpenGraphScrapper tested;

    @BeforeAll
    static void stubAllMockServerRoute() throws IOException {
        try (InputStream htmlBody = OpenGraphScrapperTest.class.getResourceAsStream("og-head-test.html")) {
            Assertions.assertThat(htmlBody).isNotNull();

            mockServer.stubFor(
                    WireMock.get(WireMock.urlMatching(".*/article\\.html"))
                            .willReturn(WireMock.aResponse()
                                    .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.TEXT_HTML_VALUE)
                                    .withStatus(HttpStatus.OK.value())
                                    .withBody(htmlBody.readAllBytes())));

        }
        Random rd = new Random();
        byte[] arr = new byte[2048];
        rd.nextBytes(arr);
        mockServer.stubFor(WireMock.get(WireMock.urlMatching(".*/podcast\\.mp3"))
                .willReturn(WireMock.aResponse()
                        .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_OCTET_STREAM_VALUE)
                        .withStatus(HttpStatus.OK.value())
                        .withBody(arr)));

        mockServer.stubFor(WireMock.get(WireMock.urlMatching(".*/not-found\\.html"))
                .willReturn(WireMock.notFound()));
        mockServer.start();
    }

    @BeforeEach
    void setUp() {
        WebClient webClient = WebClient.builder()
                .baseUrl(mockServer.baseUrl())
                .clientConnector(new ReactorClientHttpConnector(
                        HttpClient.create()
                                .followRedirect(true)
                                .compress(true)
                )).build();
        this.tested = new OpenGraphScrapper(webClient, ogReader, List.of(new YoutubeRequestPlugin()));
    }

    @Test
    void should_use_plugin_for_scrapper() {
        OpenGraph block = tested.scrap(URI.create("https://www.youtube.com/watch?v=l9nh1l8ZIJQ")).block();
        System.out.println(block);
    }
}
