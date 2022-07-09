package fr.ght1pc9kc.baywatch.scraper.api;

import fr.ght1pc9kc.baywatch.scraper.api.model.AtomFeed;
import reactor.core.publisher.Mono;

import java.net.URI;

public interface FeedScraperService {
    Mono<AtomFeed> scrapFeedHeader(URI link);
}
