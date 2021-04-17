package fr.ght1pc9kc.baywatch.infra.controllers;

import fr.ght1pc9kc.baywatch.api.FeedService;
import fr.ght1pc9kc.baywatch.api.model.Feed;
import fr.ght1pc9kc.baywatch.api.model.RawFeed;
import fr.ght1pc9kc.baywatch.domain.exceptions.BadCriteriaFilter;
import fr.ght1pc9kc.baywatch.domain.utils.Hasher;
import fr.ght1pc9kc.baywatch.infra.model.FeedForm;
import fr.ght1pc9kc.baywatch.infra.request.PageRequestFormatter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import javax.validation.Valid;
import java.net.URI;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/feeds")
public class FeedController {

    private final FeedService feedService;

    @GetMapping("/{id}")
    public Mono<Feed> get(@RequestParam("id") String id) {
        return feedService.get(id)
                .onErrorMap(BadCriteriaFilter.class, e -> new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getLocalizedMessage()));
    }

    @GetMapping
    public Flux<Feed> list(@RequestParam Map<String, String> queryStringParams) {
        return feedService.list(PageRequestFormatter.parse(queryStringParams))
                .onErrorMap(BadCriteriaFilter.class, e -> new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getLocalizedMessage()));
    }

    @PutMapping
    @PostMapping
    public Mono<ResponseEntity<Feed>> subscribe(@Valid Mono<FeedForm> feedForm) {
        return feedForm.map(form ->
                Feed.builder()
                        .raw(RawFeed.builder()
                                .id(Hasher.sha3(form.url))
                                .url(URI.create(form.url))
                                .name(form.name)
                                .build())
                        .build())
                .flatMap(feed -> feedService.persist(Collections.singleton(feed)).thenReturn(feed))
                .map(feed -> ResponseEntity.created(URI.create("/api/feeds/" + feed.getId())).body(feed));

    }

    @DeleteMapping
    public Mono<Feed> unsubscribe(String feedId) {
        return feedService.get(feedId)
                .flatMap(feed -> feedService.delete(Collections.singleton(feedId)).thenReturn(feed));
    }

    @PostMapping("/import")
    public Flux<Feed> importFeeds(@RequestBody @Valid Flux<FeedForm> feedForms) {
        return feedForms.map(form -> {
            URI url = URI.create(form.url);
            return Feed.builder()
                    .raw(RawFeed.builder()
                            .id(Hasher.sha3(form.url))
                            .url(url)
                            .name(Optional.ofNullable(form.name).orElseGet(url::getHost))
                            .build())
                    .build();
        }).collectList()
                .flatMapMany(feeds -> feedService.persist(feeds).thenMany(Flux.fromIterable(feeds)));
    }
}
