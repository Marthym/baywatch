package fr.ght1pc9kc.baywatch.techwatch.infra.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import fr.ght1pc9kc.baywatch.common.domain.Hasher;
import fr.ght1pc9kc.baywatch.common.domain.exceptions.BadRequestCriteria;
import fr.ght1pc9kc.baywatch.common.infra.model.Page;
import fr.ght1pc9kc.baywatch.techwatch.api.FeedService;
import fr.ght1pc9kc.baywatch.techwatch.api.model.WebFeed;
import fr.ght1pc9kc.baywatch.techwatch.infra.model.FeedForm;
import fr.ght1pc9kc.entity.api.Entity;
import fr.ght1pc9kc.juery.api.PageRequest;
import fr.ght1pc9kc.juery.basic.QueryStringParser;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.util.Collections;
import java.util.Optional;
import java.util.Set;

@Slf4j
@RestController
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('USER', 'MANAGER', 'ADMIN')")
@RequestMapping("${baywatch.base-route}/feeds")
public class FeedController {
    private static final URI FEED_BASE = URI.create("/feeds");
    private static final QueryStringParser qsParser = QueryStringParser.withDefaultConfig();
    private final FeedService feedService;
    private final ObjectMapper mapper;

    @GetMapping("/{id}")
    public Mono<Entity<WebFeed>> get(@PathVariable("id") String id) {
        return feedService.get(id)
                .onErrorMap(BadRequestCriteria.class, e -> new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getLocalizedMessage()));
    }

    @GetMapping
    @PreAuthorize("permitAll()")
    public Mono<Page<Entity<WebFeed>>> list(ServerHttpRequest request) {
        PageRequest pageRequest = qsParser.parse(request.getQueryParams());
        Flux<Entity<WebFeed>> feeds = feedService.list(pageRequest)
                .onErrorMap(BadRequestCriteria.class, e -> new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getLocalizedMessage()));

        return feedService.count(pageRequest)
                .map(count -> Page.of(feeds, count));
    }

    @DeleteMapping("/{id}")
    public Mono<Entity<WebFeed>> unsubscribe(@PathVariable("id") String id) {
        return feedService.get(id)
                .flatMap(feed -> feedService.delete(Collections.singleton(id)).thenReturn(feed));
    }

    @PostMapping("/import")
    public Flux<Entity<WebFeed>> importFeeds(@RequestBody @Valid Flux<FeedForm> feedForms) {
        return feedForms.map(form -> {
                    URI uri = URI.create(form.location());
                    return Entity.identify(WebFeed.builder()
                                    .location(uri)
                                    .name(Optional.ofNullable(form.name()).orElseGet(uri::getHost))
                                    .tags(Set.copyOf(form.tags()))
                                    .build())
                            .withId(Hasher.identify(uri));
                }).collectList()
                .flatMapMany(feedService::addAndSubscribe);
    }
}
