package fr.ght1pc9kc.baywatch.techwatch.infra.controllers;

import fr.ght1pc9kc.baywatch.techwatch.api.FeedService;
import fr.ght1pc9kc.baywatch.techwatch.api.model.Feed;
import fr.ght1pc9kc.baywatch.techwatch.api.model.RawFeed;
import fr.ght1pc9kc.baywatch.common.domain.exceptions.BadRequestCriteria;
import fr.ght1pc9kc.baywatch.common.domain.Hasher;
import fr.ght1pc9kc.baywatch.common.infra.model.Page;
import fr.ght1pc9kc.baywatch.techwatch.infra.model.FeedForm;
import fr.ght1pc9kc.baywatch.common.infra.model.PatchOperation;
import fr.ght1pc9kc.baywatch.common.infra.model.PatchPayload;
import fr.ght1pc9kc.juery.api.PageRequest;
import fr.ght1pc9kc.juery.basic.QueryStringParser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.support.WebExchangeBindException;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import javax.validation.Valid;
import java.net.URI;
import java.util.Collections;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('USER', 'MANAGER', 'ADMIN')")
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
    @PreAuthorize("permitAll()")
    public Mono<Page<Feed>> list(ServerHttpRequest request) {
        PageRequest pageRequest = qsParser.parse(request.getQueryParams());
        Flux<Feed> feeds = feedService.list(pageRequest)
                .onErrorMap(BadRequestCriteria.class, e -> new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getLocalizedMessage()));

        return feedService.count(pageRequest)
                .map(count -> Page.of(feeds, count));
    }

    @PatchMapping
    public Mono<Integer> bulkUpdate(@RequestBody PatchPayload patchs) {
        log.debug(patchs.toString());
        Set<String> ids = patchs.getOperations().stream()
                .filter(p -> p.op == PatchOperation.remove && p.path.getParent().toString().equals("/feeds"))
                .map(p -> p.path.getFileName().toString())
                .collect(Collectors.toUnmodifiableSet());
        return feedService.delete(ids);
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
                .map(feed -> ResponseEntity.created(URI.create("/api/feeds/" + feed.getId())).body(feed))
                .onErrorMap(WebExchangeBindException.class, e -> {
                    String message = e.getFieldErrors().stream().map(err -> err.getField() + " " + err.getDefaultMessage()).collect(Collectors.joining("\n"));
                    return new ResponseStatusException(HttpStatus.BAD_REQUEST, message);
                });

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
