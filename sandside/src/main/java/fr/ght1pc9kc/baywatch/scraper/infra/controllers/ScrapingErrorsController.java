package fr.ght1pc9kc.baywatch.scraper.infra.controllers;

import fr.ght1pc9kc.baywatch.scraper.api.ScrapingErrorsService;
import fr.ght1pc9kc.baywatch.scraper.api.model.ScrapingError;
import fr.ght1pc9kc.baywatch.techwatch.api.model.WebFeed;
import fr.ght1pc9kc.entity.api.Entity;
import lombok.RequiredArgsConstructor;
import org.springframework.graphql.data.method.annotation.BatchMapping;
import org.springframework.graphql.data.method.annotation.SchemaMapping;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('USER', 'MANAGER', 'ADMIN')")
public class ScrapingErrorsController {
    private final ScrapingErrorsService scrapingErrorsService;

    @BatchMapping(typeName = "Feed", field = "error", maxBatchSize = 50)
    public Mono<Map<Entity<WebFeed>, ScrapingError>> errors(List<Entity<WebFeed>> feeds) {
        if (feeds.isEmpty()) {
            return Mono.just(Map.of());
        }

        var entities = feeds.stream()
                .map(e -> Map.entry(e.id(), e))
                .collect(Collectors.toUnmodifiableMap(Map.Entry::getKey, Map.Entry::getValue));

        return scrapingErrorsService.list(entities.keySet())
                .map(scrapingError -> Map.entry(entities.get(scrapingError.id()), scrapingError.self()))
                .collectMap(Map.Entry::getKey, Map.Entry::getValue);
    }

    @SchemaMapping(typeName = "ScrapingError", field = "level")
    public Mono<String> computeErrorLevel(ScrapingError error) {
        return Mono.just(scrapingErrorsService.level(error).getName());
    }

    @SchemaMapping(typeName = "ScrapingError", field = "message")
    public Mono<String> filterErrorMessage(ScrapingError error) {
        return Mono.just(ScrapingErrorsService.filterMessage(error.code()));
    }
}
