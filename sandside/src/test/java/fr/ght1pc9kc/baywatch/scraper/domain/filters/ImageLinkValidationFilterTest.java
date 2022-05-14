package fr.ght1pc9kc.baywatch.scraper.domain.filters;

import fr.ght1pc9kc.baywatch.techwatch.api.model.RawNews;
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

class ImageLinkValidationFilterTest {

    @Test
    void should_validate_link_successfully() {
        ImageLinkValidationFilter tested = new ImageLinkValidationFilter(
                WebClient.builder().exchangeFunction(new SuccessExchangeFunction()).build());

        RawNews rawNews = RawNews.builder().id("0").link(URI.create("https://www.jedi.com/"))
                .image(URI.create("https://www.jedi.com/")).build();

        StepVerifier.create(tested.filter(rawNews))
                .expectNext(rawNews)
                .verifyComplete();
    }

    @Test
    void should_validate_link_with_query_parameters() {
        ImageLinkValidationFilter tested = new ImageLinkValidationFilter(
                WebClient.builder().exchangeFunction(new SuccessExchangeFunction()).build());

        RawNews rawNews = RawNews.builder().id("0").link(URI.create("https://www.jedi.com/"))
                .image(URI.create("https://www.jedi.com/image.jpg?w=42")).build();

        StepVerifier.create(tested.filter(rawNews))
                .expectNext(rawNews)
                .verifyComplete();
    }

    @Test
    void should_validate_link_with_illegal_scheme() {
        ImageLinkValidationFilter tested = new ImageLinkValidationFilter(
                WebClient.builder().exchangeFunction(new SuccessExchangeFunction()).build());

        RawNews rawNews = RawNews.builder().id("0").link(URI.create("https://www.jedi.com/"))
                .image(URI.create("file://www.jedi.com/")).build();

        StepVerifier.create(tested.filter(rawNews))
                .expectNext(rawNews.withImage(null))
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
        ImageLinkValidationFilter tested = new ImageLinkValidationFilter(
                WebClient.builder().exchangeFunction(new RedirectExchangeFunction()).build());

        RawNews rawNews = RawNews.builder().id("0").link(URI.create("https://www.jedi.com/"))
                .image(URI.create("https://www.jedi.com/")).build();

        StepVerifier.create(tested.filter(rawNews))
                .expectNext(rawNews.withImage(URI.create("https://padawan.jedi.com/?from=master")))
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
        ImageLinkValidationFilter tested = new ImageLinkValidationFilter(
                WebClient.builder().exchangeFunction(new ErrorExchangeFunction()).build());

        RawNews rawNews = RawNews.builder().id("0").link(URI.create("https://www.jedi.com/"))
                .image(URI.create("https://www.jedi.com/")).build();

        StepVerifier.create(tested.filter(rawNews))
                .expectNext(rawNews.withImage(null))
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