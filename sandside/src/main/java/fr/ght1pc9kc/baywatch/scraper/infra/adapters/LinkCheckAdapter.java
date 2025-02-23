package fr.ght1pc9kc.baywatch.scraper.infra.adapters;

import fr.ght1pc9kc.baywatch.scraper.domain.ports.LinkCheckPort;
import io.netty.handler.codec.http.HttpHeaderNames;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.util.List;
import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class LinkCheckAdapter implements LinkCheckPort {
    private final WebClient http;

    @Override
    public Mono<URI> check(URI link) {
        return http.get().uri(link).exchangeToMono(ClientResponse::toBodilessEntity)
                .flatMap(response -> {
                    if (response.getStatusCode().is2xxSuccessful()) {
                        log.trace(" SUCCESS {}", link);
                        return Mono.just(link);

                    } else if (response.getStatusCode().is3xxRedirection()) {
                        URI location = Optional.ofNullable(
                                        response.getHeaders().get(HttpHeaderNames.LOCATION.toString()))
                                .map(List::getFirst)
                                .map(URI::create)
                                .orElse(link);
                        log.trace("REDIRECT {}", location);
                        log.trace("    FROM {}", link);
                        return Mono.just(location);

                    } else {
                        log.debug("    BAD {}", link);
                        return Mono.empty();
                    }
                });
    }
}
