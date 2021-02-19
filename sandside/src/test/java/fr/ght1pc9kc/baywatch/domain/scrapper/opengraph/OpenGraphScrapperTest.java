package fr.ght1pc9kc.baywatch.domain.scrapper.opengraph;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import fr.ght1pc9kc.baywatch.domain.scrapper.opengraph.model.OGType;
import fr.ght1pc9kc.baywatch.domain.scrapper.opengraph.model.OpenGraph;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;

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
        }

        mockServer.stubFor(WireMock.get(WireMock.urlMatching(".*/not-found\\.html"))
                .willReturn(WireMock.notFound()));
        mockServer.start();
    }

    @AfterAll
    static void afterAll() {
        mockServer.stop();
    }

    @Test
    void should_parse_opengraph() throws MalformedURLException {
        URI page = URI.create(mockServer.baseUrl() + "/article.html");
        OpenGraph actual = tested.scrap(page).block();

        Assertions.assertThat(actual).isEqualTo(OpenGraph.builder()
                .title("De Paris à Toulouse")
                .description("Déplacement des serveurs de l’infrastructure i-Run depuis Paris jusqu’à Toulouse chez " +
                        "notre hébergeur FullSave. Nouvelles machines, nouvelle infra pour plus de résilience et une " +
                        "meilleure tenue de la charge sur les sites publics comme sur le backoffice.")
                .type(OGType.ARTICLE)
                .url(new URL("https://blog.i-run.si/posts/silife/infra-de-paris-a-toulouse/"))
                .image(URI.create("https://blog.i-run.si/posts/silife/infra-de-paris-a-toulouse/featured.jpg"))
                .build());
    }

    @Test
    void should_scrap_not_found() {
        URI page = URI.create(mockServer.baseUrl() + "/not-found.html");
        OpenGraph actual = tested.scrap(page).block();
        Assertions.assertThat(actual).isEqualTo(Mono.empty().block());
    }
}