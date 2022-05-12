package fr.ght1pc9kc.baywatch.scraper.infra.controllers;

import fr.ght1pc9kc.baywatch.scraper.api.NewsEnrichmentService;
import fr.ght1pc9kc.baywatch.techwatch.api.model.News;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.net.URI;

@RestController
@RequiredArgsConstructor
@RequestMapping("/scrap")
public class ScrapController {

    private final NewsEnrichmentService newsEnrichmentService;

    @PostMapping("/news/{uri}")
    public Mono<News> scrapSimpleNews(@RequestParam("uri") URI uri) {
        return newsEnrichmentService.buildStandaloneNews(uri)
                .flatMap(newsEnrichmentService::applyNewsFilters)
                .flatMap(newsEnrichmentService::saveAndShare);
    }
}
