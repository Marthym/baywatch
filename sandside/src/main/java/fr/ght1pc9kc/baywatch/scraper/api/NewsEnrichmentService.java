package fr.ght1pc9kc.baywatch.scraper.api;

import fr.ght1pc9kc.baywatch.techwatch.api.model.News;
import reactor.core.publisher.Mono;

import java.net.URI;

public interface NewsEnrichmentService {
    Mono<News> buildStandaloneNews(URI uri);

    Mono<News> applyNewsFilters(News news);

    Mono<News> saveAndShare(News news);
}
