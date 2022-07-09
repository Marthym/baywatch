package fr.ght1pc9kc.baywatch.techwatch.domain.ports;

import fr.ght1pc9kc.baywatch.techwatch.api.model.Feed;
import reactor.core.publisher.Mono;

import java.net.URI;

public interface ScraperServicePort {
    Mono<Feed> fetchFeedData(URI link);
}
