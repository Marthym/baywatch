package fr.ght1pc9kc.baywatch.indexer.infra.controllers;

import fr.ght1pc9kc.baywatch.common.domain.exceptions.BadRequestCriteria;
import fr.ght1pc9kc.baywatch.indexer.api.FeedSearchService;
import fr.ght1pc9kc.baywatch.indexer.infra.model.IndexEntry;
import lombok.RequiredArgsConstructor;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Flux;

@Controller
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('USER', 'MANAGER', 'ADMIN')")
public class SearchIndexController {
    private final FeedSearchService feedSearchService;

    @QueryMapping
    public Flux<IndexEntry> searchIndex(@Argument("q") String q) {
        return feedSearchService.search(q)
                .map(id -> new IndexEntry(id, "FEED"))
                .onErrorMap(BadRequestCriteria.class, e -> new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getLocalizedMessage()));
    }
}
