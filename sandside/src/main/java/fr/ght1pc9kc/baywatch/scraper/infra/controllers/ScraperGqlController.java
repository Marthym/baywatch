package fr.ght1pc9kc.baywatch.scraper.infra.controllers;

import fr.ght1pc9kc.baywatch.scraper.api.FeedScraperService;
import fr.ght1pc9kc.baywatch.scraper.api.NewsEnrichmentService;
import fr.ght1pc9kc.baywatch.scraper.api.model.AtomEntry;
import fr.ght1pc9kc.baywatch.scraper.api.model.AtomFeed;
import fr.ght1pc9kc.baywatch.scraper.infra.config.ScraperMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import reactor.core.publisher.Mono;

import java.net.URI;

@Controller
@RequiredArgsConstructor
public class ScraperGqlController {
    private final NewsEnrichmentService newsEnrichmentService;
    private final FeedScraperService feedScraperService;
    private final ScraperMapper mapper;

    @MutationMapping
    @PreAuthorize("hasAnyRole('USER', 'MANAGER', 'ADMIN')")
    public Mono<AtomEntry> scrapSimpleNews(@Argument URI uri) {
        return newsEnrichmentService.buildStandaloneNews(uri)
                .flatMap(newsEnrichmentService::applyNewsFilters)
                .flatMap(newsEnrichmentService::saveAndShare)
                .map(mapper::getAtomFromNews);
    }

    @QueryMapping
    @PreAuthorize("hasAnyRole('USER', 'MANAGER', 'ADMIN')")
    public Mono<AtomFeed> scrapFeedHeader(@Argument URI link) {
        return feedScraperService.scrapFeedHeader(link);
    }
}
