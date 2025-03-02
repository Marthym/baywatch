package fr.ght1pc9kc.baywatch.scraper.domain.ports;

import reactor.core.publisher.Mono;

import java.net.URI;

public interface LinkCheckPort {
    Mono<URI> check(URI link);
}
