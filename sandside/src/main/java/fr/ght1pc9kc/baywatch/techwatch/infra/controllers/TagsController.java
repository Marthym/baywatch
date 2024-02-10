package fr.ght1pc9kc.baywatch.techwatch.infra.controllers;

import fr.ght1pc9kc.baywatch.common.domain.exceptions.BadRequestCriteria;
import fr.ght1pc9kc.baywatch.techwatch.api.FeedService;
import fr.ght1pc9kc.juery.api.PageRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

@RestController
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('USER', 'MANAGER', 'ADMIN')")
@RequestMapping("${baywatch.base-route}/tags")
public class TagsController {
    private final FeedService feedService;

    @GetMapping
    public Mono<List<String>> list() {
        return feedService.list(PageRequest.all())
                .flatMap(f -> Flux.fromIterable(f.self().tags()))
                .distinct()
                .sort()
                .collectList()
                .onErrorMap(BadRequestCriteria.class, e -> new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getLocalizedMessage()));
    }
}
