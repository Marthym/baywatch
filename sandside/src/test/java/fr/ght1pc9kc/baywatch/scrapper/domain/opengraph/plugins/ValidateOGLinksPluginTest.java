package fr.ght1pc9kc.baywatch.scrapper.domain.opengraph.plugins;

import fr.ght1pc9kc.scraphead.core.model.opengraph.OpenGraph;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.ExchangeFunction;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.net.URI;

class ValidateOGLinksPluginTest {

    @Test
    void should_validate_link_successfully() {
        ValidateOGLinksPlugin tested = new ValidateOGLinksPlugin(
                WebClient.builder().exchangeFunction(new SuccessExchangeFunction()).build());

        OpenGraph openGraph = OpenGraph.builder()
                .image(URI.create("https://www.jedi.com/"))
                .build();
        Mono<OpenGraph> actual = tested.postTreatment(openGraph);

        StepVerifier.create(actual)
                .expectNext(openGraph)
                .verifyComplete();
    }

    public static final class SuccessExchangeFunction implements ExchangeFunction {
        @Override
        public @NotNull Mono<ClientResponse> exchange(@NotNull ClientRequest request) {
            return Mono.just(ClientResponse.create(HttpStatus.OK)
                    .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_OCTET_STREAM_VALUE)
                    .build());
        }
    }

    @Test
    void should_validate_link_redirection() {
        ValidateOGLinksPlugin tested = new ValidateOGLinksPlugin(
                WebClient.builder().exchangeFunction(new RedirectExchangeFunction()).build());

        OpenGraph openGraph = OpenGraph.builder()
                .image(URI.create("https://www.jedi.com/"))
                .build();
        Mono<OpenGraph> actual = tested.postTreatment(openGraph);

        StepVerifier.create(actual)
                .expectNext(openGraph.withImage(URI.create("https://padawan.jedi.com/?from=master")))
                .verifyComplete();
    }

    public static final class RedirectExchangeFunction implements ExchangeFunction {
        @Override
        public @NotNull Mono<ClientResponse> exchange(@NotNull ClientRequest request) {
            return Mono.just(ClientResponse.create(HttpStatus.PERMANENT_REDIRECT)
                    .header(HttpHeaders.LOCATION, "https://padawan.jedi.com/?from=master")
                    .build());
        }
    }

    @Test
    void should_validate_link_error() {
        ValidateOGLinksPlugin tested = new ValidateOGLinksPlugin(
                WebClient.builder().exchangeFunction(new ErrorExchangeFunction()).build());

        OpenGraph openGraph = OpenGraph.builder()
                .image(URI.create("https://www.jedi.com/"))
                .build();
        Mono<OpenGraph> actual = tested.postTreatment(openGraph);

        StepVerifier.create(actual)
                .expectNext(openGraph.withImage(null))
                .verifyComplete();
    }

    public static final class ErrorExchangeFunction implements ExchangeFunction {
        @Override
        public @NotNull Mono<ClientResponse> exchange(@NotNull ClientRequest request) {
            return Mono.just(ClientResponse.create(HttpStatus.NOT_FOUND)
                    .build());
        }
    }
}