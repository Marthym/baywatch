package fr.ght1pc9kc.baywatch.scraper.infra.controllers;

import fr.ght1pc9kc.baywatch.scraper.api.ScrapEnrichmentService;
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

    private final ScrapEnrichmentService scrapEnrichmentService;

    @PostMapping("/news/{uri}")
    @PreAuthorize("hasAnyRole('USER', 'MANAGER', 'ADMIN')")
    public Mono<Void> scrapSimpleNews(@PathVariable("uri") URI uri) {
        return scrapEnrichmentService.scrapSingleNews(uri);
    }
}
