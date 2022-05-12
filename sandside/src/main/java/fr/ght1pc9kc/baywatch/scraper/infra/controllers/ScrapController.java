package fr.ght1pc9kc.baywatch.scraper.infra.controllers;

import fr.ght1pc9kc.baywatch.scraper.api.NewsEnrichmentService;
import fr.ght1pc9kc.baywatch.techwatch.api.model.News;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.net.URI;

@RestController
@RequiredArgsConstructor
@RequestMapping("${baywatch.base-route}/scrap")
public class ScrapController {

    private final NewsEnrichmentService newsEnrichmentService;

    @PostMapping("/news/{uri}")
    @PreAuthorize("hasAnyRole('USER', 'MANAGER', 'ADMIN')")
    public Mono<News> scrapSimpleNews(@PathVariable("uri") URI uri) {
        return newsEnrichmentService.buildStandaloneNews(uri)
                .flatMap(newsEnrichmentService::applyNewsFilters)
                .flatMap(newsEnrichmentService::saveAndShare);
    }
}
