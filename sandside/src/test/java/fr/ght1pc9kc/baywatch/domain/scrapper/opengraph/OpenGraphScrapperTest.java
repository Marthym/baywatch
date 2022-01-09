package fr.ght1pc9kc.baywatch.domain.scrapper.opengraph;

import com.machinezoo.noexception.Exceptions;
import fr.ght1pc9kc.baywatch.domain.scrapper.opengraph.model.OGType;
import fr.ght1pc9kc.baywatch.domain.scrapper.opengraph.model.OpenGraph;
import org.assertj.core.api.Assertions;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.core.io.buffer.DefaultDataBufferFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.ExchangeFunction;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.List;
import java.util.Locale;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.spy;

class OpenGraphScrapperTest {
    private final OpenGraphMetaReader ogReader = spy(new OpenGraphMetaReader());
    private final WebClient webClient = WebClient.builder()
            .exchangeFunction(new MockExchangeFunction()).build();
    private final OpenGraphScrapper tested = new OpenGraphScrapper(webClient, ogReader, List.of());

    @BeforeEach
    void setUp() {
        reset(ogReader);
    }

    @Test
    void should_parse_opengraph() throws MalformedURLException {
        URI page = URI.create("https://blog.ght1pc9kc.fr/og-head-test.html");
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
    void should_parse_opengraph_with_empty_fields() throws MalformedURLException {
        URI page = URI.create("https://blog.ght1pc9kc.fr/ght-bad-parsing.html");
        OpenGraph actual = tested.scrap(page).block();

        Assertions.assertThat(actual).isEqualTo(OpenGraph.builder()
                .title("Les Critères de recherche avec Juery")
                .type(OGType.ARTICLE)
                .url(new URL("https://blog.ght1pc9kc.fr/2021/les-crit%C3%A8res-de-recherche-avec-juery.html"))
                .build());
    }

    @Test
    void should_parse_opengraph_with_apostrophe() {
        URI page = URI.create("https://blog.ght1pc9kc.fr/apostrophe.html");
        OpenGraph actual = tested.scrap(page).block();

        Assertions.assertThat(actual).isNotNull();
        assertAll(
                () -> Assertions.assertThat(actual.title).isEqualTo("Économiseur d'écran personnalisé avec XSecureLock"),
                () -> Assertions.assertThat(actual.type).isEqualTo(OGType.ARTICLE),
                () -> Assertions.assertThat(actual.image).isEqualTo(URI.create("https://d1g3mdmxf8zbo9.cloudfront.net/images/i3/xsecurelock@2x.jpg")),
                () -> Assertions.assertThat(actual.locale).isEqualTo(Locale.FRANCE)
        );
    }

    @Test
    void should_scrap_not_found() {
        URI page = URI.create("https://blog.ght1pc9kc.fr/not-found.html");
        OpenGraph actual = tested.scrap(page).block();
        Assertions.assertThat(actual).isNull();
    }

    @Test
    void should_scrap_not_html() {
        URI page = URI.create("https://blog.ght1pc9kc.fr/podcast.mp3");
        Mono<OpenGraph> actual = tested.scrap(page);

        StepVerifier.create(actual).verifyComplete();
    }

    public static final class MockExchangeFunction implements ExchangeFunction {
        @Override
        public @NotNull Mono<ClientResponse> exchange(@NotNull ClientRequest request) {
            DefaultDataBufferFactory factory = new DefaultDataBufferFactory();

            if (!request.url().getPath().endsWith(".html")) {
                Random rd = new Random();
                byte[] arr = new byte[2048];
                rd.nextBytes(arr);
                return Mono.just(ClientResponse.create(HttpStatus.OK)
                        .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_OCTET_STREAM_VALUE)
                        .body(Flux.just(factory.wrap(arr)))
                        .build());
            }

            InputStream is = OpenGraphScrapperTest.class.getResourceAsStream(request.url().getPath().replaceAll("^/", ""));
            if (is == null) {
                return Mono.just(ClientResponse.create(HttpStatus.NOT_FOUND).build());
            }

            Flux<DataBuffer> data = DataBufferUtils.readInputStream(() -> is, factory, 1024)
                    .doAfterTerminate(Exceptions.wrap().runnable(is::close));
            return Mono.just(ClientResponse.create(HttpStatus.OK)
                    .header(HttpHeaders.CONTENT_TYPE, MediaType.TEXT_HTML_VALUE)
                    .body(data)
                    .build());

        }
    }
}