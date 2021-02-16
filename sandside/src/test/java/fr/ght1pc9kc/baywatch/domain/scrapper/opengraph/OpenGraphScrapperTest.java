package fr.ght1pc9kc.baywatch.domain.scrapper.opengraph;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import fr.ght1pc9kc.baywatch.domain.scrapper.opengraph.model.OpenGraph;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;

class OpenGraphScrapperTest {
    static final WireMockServer mockServer = new WireMockServer(WireMockConfiguration.wireMockConfig().dynamicPort());

    private final OpenGraphScrapper tested = new OpenGraphScrapper();

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

            mockServer.start();
        }
    }

    @AfterAll
    static void afterAll() {
        mockServer.stop();
    }

    @Test
    void should_parse_opengraph() {
        URI page = URI.create(mockServer.baseUrl() + "/article.html");
        OpenGraph actual = tested.scrap(page).block();

        Assertions.assertThat(actual).isEqualTo(OpenGraph.builder().build());
    }
}