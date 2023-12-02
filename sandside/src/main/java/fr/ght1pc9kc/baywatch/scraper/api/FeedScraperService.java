package fr.ght1pc9kc.baywatch.scraper.api;

import fr.ght1pc9kc.baywatch.scraper.api.model.AtomFeed;
import fr.ght1pc9kc.baywatch.scraper.api.model.ScrapResult;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.time.Period;

public interface FeedScraperService {
    Mono<AtomFeed> scrapFeedHeader(URI link);

    Mono<ScrapResult> scrap(Period maxRetention);

    void dispose();
}
