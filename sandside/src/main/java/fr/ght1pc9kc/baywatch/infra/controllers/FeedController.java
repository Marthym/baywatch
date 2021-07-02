package fr.ght1pc9kc.baywatch.infra.controllers;

import fr.ght1pc9kc.baywatch.api.FeedService;
import fr.ght1pc9kc.baywatch.api.model.Feed;
import fr.ght1pc9kc.baywatch.api.model.RawFeed;
import fr.ght1pc9kc.baywatch.domain.exceptions.BadCriteriaFilter;
import fr.ght1pc9kc.baywatch.domain.utils.Hasher;
import fr.ght1pc9kc.baywatch.infra.model.FeedForm;
import fr.ght1pc9kc.baywatch.infra.request.pagination.Page;
import fr.ght1pc9kc.juery.api.PageRequest;
import fr.ght1pc9kc.juery.basic.PageRequestFormatter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import javax.validation.Valid;
import java.net.URI;
import java.util.Collections;
import java.util.Optional;

@RestController
@RequiredArgsConstructor
@RequestMapping("${baywatch.base-route}/feeds")
public class FeedController {

    private final FeedService feedService;

    @GetMapping("/{id}")
    public Mono<Feed> get(@RequestParam("id") String id) {
        return feedService.get(id)
                .onErrorMap(BadCriteriaFilter.class, e -> new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getLocalizedMessage()));
    }

    @GetMapping
    public Mono<Page<Feed>> list(@RequestParam MultiValueMap<String, String> queryStringParams) {
        PageRequest pageRequest = PageRequestFormatter.parse(queryStringParams);
        Flux<Feed> feeds = feedService.list(pageRequest)
                .onErrorMap(BadCriteriaFilter.class, e -> new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getLocalizedMessage()));
        // Link: <https://api.github.com/repos?page=3&per_page=100>; rel="next",
        //<https://api.github.com/repos?page=50&per_page=100>; rel="last"

        return feedService.count(pageRequest)
                .map(count -> {
                    int pageCount = (int) Math.ceil((double) count / pageRequest.pagination().size());
                    return pageCount;
                }).map(count -> {
                    HttpHeaders httpHeaders = new HttpHeaders();
                    httpHeaders.add("Link", "<https://api.github.com/repos?_p=" + pageRequest.pagination().page() + 1 + "&per_page=" + pageRequest.pagination().size() + ">; rel=\"next\"," +
                            "<https://api.github.com/repos?_p=" + count + "&per_page=" + pageRequest.pagination().size() + ">; rel=\"last\"");
                    return new Page<>(HttpStatus.OK, httpHeaders, feeds);
                });
    }

    @PutMapping
    @PostMapping
    public Mono<ResponseEntity<Feed>> subscribe(@Valid Mono<FeedForm> feedForm) {
        return feedForm.map(form -> {
            URI uri = URI.create(form.url);
            return Feed.builder()
                    .raw(RawFeed.builder()
                            .id(Hasher.identify(uri))
                            .url(uri)
                            .name(form.name)
                            .build())
                    .build();
        })
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
            URI uri = URI.create(form.url);
            return Feed.builder()
                    .raw(RawFeed.builder()
                            .id(Hasher.identify(uri))
                            .url(uri)
                            .name(Optional.ofNullable(form.name).orElseGet(uri::getHost))
                            .build())
                    .build();
        }).collectList()
                .flatMapMany(feeds -> feedService.persist(feeds).thenMany(Flux.fromIterable(feeds)));
    }
}
