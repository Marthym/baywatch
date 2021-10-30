package fr.ght1pc9kc.baywatch.infra.controllers;

import fr.ght1pc9kc.baywatch.api.FeedService;
import fr.ght1pc9kc.baywatch.api.model.Feed;
import fr.ght1pc9kc.baywatch.api.model.RawFeed;
import fr.ght1pc9kc.baywatch.domain.exceptions.BadRequestCriteria;
import fr.ght1pc9kc.baywatch.domain.utils.Hasher;
import fr.ght1pc9kc.baywatch.infra.http.pagination.Page;
import fr.ght1pc9kc.baywatch.infra.model.FeedForm;
import fr.ght1pc9kc.juery.api.PageRequest;
import fr.ght1pc9kc.juery.basic.QueryStringParser;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import javax.validation.Valid;
import java.net.URI;
import java.util.Collections;
import java.util.Optional;
import java.util.Set;

@RestController
@RequiredArgsConstructor
@RequestMapping("${baywatch.base-route}/feeds")
public class FeedController {

    private static final QueryStringParser qsParser = QueryStringParser.withDefaultConfig();
    private final FeedService feedService;

    @GetMapping("/{id}")
    public Mono<Feed> get(@PathVariable("id") String id) {
        return feedService.get(id)
                .onErrorMap(BadRequestCriteria.class, e -> new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getLocalizedMessage()));
    }

    @GetMapping
    public Mono<Page<Feed>> list(ServerHttpRequest request) {
        PageRequest pageRequest = qsParser.parse(request.getQueryParams());
        Flux<Feed> feeds = feedService.list(pageRequest)
                .onErrorMap(BadRequestCriteria.class, e -> new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getLocalizedMessage()));

        return feedService.count(pageRequest)
                .map(count -> Page.of(feeds, count));
    }

    @PutMapping("/{id}")
    public Mono<Feed> update(@PathVariable("id") String id, @Valid @RequestBody Mono<FeedForm> feedForm) {
        return feedForm.map(ff -> {
                    URI uri = URI.create(ff.url);
                    if (!id.equals(Hasher.identify(uri))) {
                        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Inconsistent ID for URL !");
                    }
                    return Feed.builder()
                            .raw(RawFeed.builder()
                                    .id(id)
                                    .url(uri)
                                    .name(ff.name)
                                    .build())
                            .tags(Set.of(ff.tags))
                            .name(ff.name)
                            .build();
                }).flatMap(feedService::update)
                .onErrorMap(BadRequestCriteria.class, e -> new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getLocalizedMessage()));
    }

    @RequestMapping(method = {RequestMethod.POST, RequestMethod.PUT})
    public Mono<ResponseEntity<Feed>> subscribe(@Valid @RequestBody Mono<FeedForm> feedForm) {
        return feedForm.map(form -> {
                    URI uri = URI.create(form.url);
                    Set<String> tags = Optional.ofNullable(form.tags).map(Set::of).orElseGet(Set::of);
                    return Feed.builder()
                            .raw(RawFeed.builder()
                                    .id(Hasher.identify(uri))
                                    .url(uri)
                                    .name(form.name)
                                    .build())
                            .tags(tags)
                            .name(form.name)
                            .build();
                })
                .flatMap(feed -> feedService.persist(Collections.singleton(feed)).thenReturn(feed))
                .map(feed -> ResponseEntity.created(URI.create("/api/feeds/" + feed.getId())).body(feed));

    }

    @DeleteMapping("/{id}")
    public Mono<Feed> unsubscribe(@PathVariable("id") String id) {
        return feedService.get(id)
                .flatMap(feed -> feedService.delete(Collections.singleton(id)).thenReturn(feed));
    }

    @PostMapping("/import")
    public Flux<Feed> importFeeds(@RequestBody @Valid Flux<FeedForm> feedForms) {
        return feedForms.map(form -> {
                    URI uri = URI.create(form.url);
                    return Feed.builder()
                            .raw(RawFeed.builder()
                                    .id(Hasher.identify(uri))
                                    .url(uri)
                                    .name(Optional.ofNullable(form.name).orElseGet(uri::getHost))
                                    .build())
                            .tags(Set.of(form.tags))
                            .build();
                }).collectList()
                .flatMapMany(feeds -> feedService.persist(feeds).thenMany(Flux.fromIterable(feeds)));
    }
}
