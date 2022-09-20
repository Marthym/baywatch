package fr.ght1pc9kc.baywatch.techwatch.infra.controllers;

import fr.ght1pc9kc.baywatch.common.api.model.EntitiesProperties;
import fr.ght1pc9kc.baywatch.common.domain.exceptions.BadRequestCriteria;
import fr.ght1pc9kc.baywatch.common.infra.model.Page;
import fr.ght1pc9kc.baywatch.techwatch.api.FeedService;
import fr.ght1pc9kc.baywatch.techwatch.api.PopularNewsService;
import fr.ght1pc9kc.baywatch.techwatch.api.model.Feed;
import fr.ght1pc9kc.baywatch.techwatch.api.model.News;
import fr.ght1pc9kc.baywatch.techwatch.api.model.Popularity;
import fr.ght1pc9kc.baywatch.techwatch.infra.model.graphql.SearchFeedsRequest;
import fr.ght1pc9kc.juery.api.PageRequest;
import fr.ght1pc9kc.juery.basic.QueryStringParser;
import lombok.RequiredArgsConstructor;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.Arguments;
import org.springframework.graphql.data.method.annotation.BatchMapping;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.graphql.data.method.annotation.SchemaMapping;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Controller
@RequiredArgsConstructor
public class GraphQLFeedsController {
    private static final QueryStringParser qsParser = QueryStringParser.withDefaultConfig();
    private final FeedService feedService;
    private final PopularNewsService popularService;

    @QueryMapping
    public Mono<Feed> getFeed(@Argument("id") String id) {
        return feedService.get(id)
                .onErrorMap(BadRequestCriteria.class, e -> new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getLocalizedMessage()));
    }

    @QueryMapping
    public Mono<Page<Feed>> feedsSearch(@Arguments SearchFeedsRequest request) {
        PageRequest pageRequest = qsParser.parse(request.toPageRequest());
        Flux<Feed> feeds = feedService.list(pageRequest);

        return feedService.count(pageRequest)
                .map(count -> Page.of(feeds, count));
    }

    @SchemaMapping(typeName = "SearchFeedsResponse")
    public Flux<News> entities(Page<News> searchNewsResponse) {
        return searchNewsResponse.getBody();
    }

    @SchemaMapping(typeName = "SearchFeedsResponse")
    public Mono<Integer> totalCount(Page<News> searchNewsResponse) {
        return Mono.justOrEmpty(searchNewsResponse.getHeaders().get("X-Total-Count"))
                .map(h -> h.get(0))
                .map(Integer::parseInt)
                .switchIfEmpty(Mono.just(0));
    }

    @BatchMapping()
    public Mono<Map<News, List<Feed>>> feeds(List<News> news) {
        if (news.isEmpty()) {
            return Mono.just(Map.of());
        }
        List<String> feedsIds = news.stream().flatMap(n -> n.getFeeds().stream()).distinct().toList();
        PageRequest pageRequest = qsParser.parse(Map.of(EntitiesProperties.ID, feedsIds));
        return feedService.list(pageRequest).collectList()
                .map(feeds -> feeds.stream().collect(Collectors.toUnmodifiableMap(Feed::getId, Function.identity())))
                .map(feeds -> news.stream()
                        .collect(Collectors.toUnmodifiableMap(Function.identity(), n -> n.getFeeds().stream()
                                .filter(feeds::containsKey)
                                .map(feeds::get).toList())));
    }

    @BatchMapping
    public Mono<Map<News, Popularity>> popularity(List<News> news) {
        if (news.isEmpty()) {
            return Mono.just(Map.of());
        }
        List<String> newsIds = news.stream().map(News::getId).toList();
        return popularService.get(newsIds).collect(Collectors.toUnmodifiableMap(Popularity::id, Function.identity()))
                .map(pops -> news.stream()
                        .filter(n -> pops.containsKey(n.getId()))
                        .collect(Collectors.toUnmodifiableMap(Function.identity(), n -> pops.get(n.getId()))));
    }

    @MutationMapping
    public Mono<Feed> subscribe(String id, String name, URI url, Collection<String> tags) {
//        if (Objects.nonNull(id)) {
//
//        } else {
//
//        }
//        feedForm.map(form -> {
//                    URI uri = URI.create(form.url);
//                    Set<String> tags = Optional.ofNullable(form.tags).map(Set::of).orElseGet(Set::of);
//                    return Feed.builder()
//                            .raw(RawFeed.builder()
//                                    .id(Hasher.identify(uri))
//                                    .url(uri)
//                                    .name(form.name)
//                                    .build())
//                            .tags(tags)
//                            .name(form.name)
//                            .build();
//                })
//                .flatMap(feed -> feedService.persist(Collections.singleton(feed)).thenReturn(feed))
//                .map(feed -> ResponseEntity.created(URI.create("/api/feeds/" + feed.getId())).body(feed))
//                .onErrorMap(WebExchangeBindException.class, e -> {
//                    String message = e.getFieldErrors().stream().map(err -> err.getField() + " " + err.getDefaultMessage()).collect(Collectors.joining("\n"));
//                    return new ResponseStatusException(HttpStatus.BAD_REQUEST, message);
//                });
        return null;
    }
}
