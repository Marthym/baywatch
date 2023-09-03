package fr.ght1pc9kc.baywatch.techwatch.domain.ports;

import fr.ght1pc9kc.baywatch.techwatch.api.model.WebFeed;
import reactor.core.publisher.Mono;

import java.net.URI;

public interface ScraperServicePort {
    Mono<WebFeed> fetchFeedData(URI link);
}
