package fr.ght1pc9kc.baywatch.techwatch.infra.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import fr.ght1pc9kc.baywatch.common.domain.Hasher;
import fr.ght1pc9kc.baywatch.common.domain.exceptions.BadRequestCriteria;
import fr.ght1pc9kc.baywatch.common.infra.model.Page;
import fr.ght1pc9kc.baywatch.common.infra.model.PatchPayload;
import fr.ght1pc9kc.baywatch.common.infra.model.ResourcePatch;
import fr.ght1pc9kc.baywatch.techwatch.api.FeedService;
import fr.ght1pc9kc.baywatch.techwatch.api.model.Feed;
import fr.ght1pc9kc.baywatch.techwatch.api.model.RawFeed;
import fr.ght1pc9kc.baywatch.techwatch.infra.model.FeedForm;
import fr.ght1pc9kc.juery.api.PageRequest;
import fr.ght1pc9kc.juery.basic.QueryStringParser;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.support.WebExchangeBindException;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

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
    public Flux<URI> bulkUpdate(@RequestBody PatchPayload patchs) {
        log.debug(patchs.toString());
        List<Mono<URI>> operations = new ArrayList<>(patchs.getResources().size());

        for (ResourcePatch resource : patchs.getResources()) {
            if (!resource.path().getPath().startsWith(FEED_BASE.getPath())) {
                continue;
            }
            switch (resource.op()) {
                case remove:
                    try {
                        String id = FEED_BASE.relativize(resource.path()).toString();
                        operations.add(feedService.delete(List.of(id)).map(deleted -> resource.path()));
                    } catch (Exception e) {
                        return Flux.error(new IllegalArgumentException("Malformed PATCH (remove) request !", e));
                    }
                    break;

                case add:
                    try {
                        FeedForm toPersist = mapper.readerFor(FeedForm.class).readValue(resource.value(), FeedForm.class);
                        Mono<URI> persisted = subscribe(Mono.just(toPersist))
                                .map(re -> URI.create(FEED_BASE.getPath() + "/" + Objects.requireNonNull(re.getBody()).getId()));
                        operations.add(persisted);
                        break;
                    } catch (IOException e) {
                        return Flux.error(new IllegalArgumentException("Malformed PATCH (add) request !", e));
                    }

                case replace:
                    try {
                        String id = FEED_BASE.relativize(resource.path()).toString();
                        FeedForm toPersist = mapper.readerFor(FeedForm.class).readValue(resource.value(), FeedForm.class);
                        Mono<URI> persisted = update(id, Mono.just(toPersist))
                                .map(re -> URI.create(FEED_BASE.getPath() + "/" + re.getId()));
                        operations.add(persisted);
                        break;
                    } catch (IOException e) {
                        return Flux.error(new IllegalArgumentException("Malformed PATCH (replace) request !", e));
                    }

                default:
                    return Flux.error(new IllegalArgumentException("Unsupported PATCH operation !"));
            }
        }

        return Flux.concat(operations);
    }

    @PutMapping("/{id}")
    public Mono<Feed> update(@PathVariable("id") String id, @Valid @RequestBody Mono<FeedForm> feedForm) {
        return feedForm.map(ff -> {
                    URI uri = URI.create(ff.url());
                    if (!id.equals(Hasher.identify(uri))) {
                        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Inconsistent ID for URL !");
                    }
                    return Feed.builder()
                            .raw(RawFeed.builder()
                                    .id(id)
                                    .url(uri)
                                    .name(ff.name())
                                    .build())
                            .tags(Set.of(ff.tags()))
                            .name(ff.name())
                            .build();
                }).flatMap(feedService::update)
                .onErrorMap(BadRequestCriteria.class, e -> new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getLocalizedMessage()));
    }

    @RequestMapping(method = {RequestMethod.POST, RequestMethod.PUT})
    public Mono<ResponseEntity<Feed>> subscribe(@Valid @RequestBody Mono<FeedForm> feedForm) {
        return feedForm.map(form -> {
                    URI uri = URI.create(form.url());
                    Set<String> tags = Optional.ofNullable(form.tags()).map(Set::of).orElseGet(Set::of);
                    return Feed.builder()
                            .raw(RawFeed.builder()
                                    .id(Hasher.identify(uri))
                                    .url(uri)
                                    .name(form.name())
                                    .build())
                            .tags(tags)
                            .name(form.name())
                            .build();
                })
                .flatMap(feed -> feedService.addAndSubscribe(Collections.singleton(feed)).next())
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
                    URI uri = URI.create(form.url());
                    return Feed.builder()
                            .raw(RawFeed.builder()
                                    .id(Hasher.identify(uri))
                                    .url(uri)
                                    .name(Optional.ofNullable(form.name()).orElseGet(uri::getHost))
                                    .build())
                            .tags(Set.of(form.tags()))
                            .build();
                }).collectList()
                .flatMapMany(feedService::addAndSubscribe);
    }
}
