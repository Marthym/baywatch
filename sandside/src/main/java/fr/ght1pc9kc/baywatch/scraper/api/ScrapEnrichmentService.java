package fr.ght1pc9kc.baywatch.scraper.api;

import fr.ght1pc9kc.baywatch.scraper.api.model.AtomFeed;
import fr.ght1pc9kc.baywatch.techwatch.api.model.News;
import reactor.core.publisher.Mono;

import java.net.URI;

public interface ScrapEnrichmentService {
    Mono<News> buildStandaloneNews(URI uri);

    Mono<News> applyNewsFilters(News news);

    Mono<News> saveAndShare(News news);

    Mono<AtomFeed> applyFeedsFilters(AtomFeed feed);
}