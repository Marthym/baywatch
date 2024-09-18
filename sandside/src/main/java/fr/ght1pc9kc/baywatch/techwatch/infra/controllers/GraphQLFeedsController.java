package fr.ght1pc9kc.baywatch.techwatch.infra.controllers;

import fr.ght1pc9kc.baywatch.common.api.model.EntitiesProperties;
import fr.ght1pc9kc.baywatch.common.domain.Hasher;
import fr.ght1pc9kc.baywatch.common.domain.exceptions.BadRequestCriteria;
import fr.ght1pc9kc.baywatch.common.infra.model.Page;
import fr.ght1pc9kc.baywatch.techwatch.api.FeedService;
import fr.ght1pc9kc.baywatch.techwatch.api.PopularNewsService;
import fr.ght1pc9kc.baywatch.techwatch.api.model.News;
import fr.ght1pc9kc.baywatch.techwatch.api.model.Popularity;
import fr.ght1pc9kc.baywatch.techwatch.api.model.WebFeed;
import fr.ght1pc9kc.baywatch.techwatch.infra.model.FeedForm;
import fr.ght1pc9kc.baywatch.techwatch.infra.model.graphql.SearchFeedsRequest;
import fr.ght1pc9kc.entity.api.Entity;
import fr.ght1pc9kc.juery.api.PageRequest;
import fr.ght1pc9kc.juery.basic.QueryStringParser;
import graphql.ErrorClassification;
import graphql.GraphqlErrorException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.Arguments;
import org.springframework.graphql.data.method.annotation.BatchMapping;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.graphql.data.method.annotation.SchemaMapping;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.util.Objects.isNull;

@Controller
@RequiredArgsConstructor
public class GraphQLFeedsController {
    private static final QueryStringParser qsParser = QueryStringParser.withDefaultConfig();
    private final FeedService feedService;
    private final PopularNewsService popularService;

    @QueryMapping
    public Mono<Entity<WebFeed>> getFeed(@Argument("id") String id) {
        return feedService.get(id)
                .onErrorMap(BadRequestCriteria.class, e -> new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getLocalizedMessage()));
    }

    @QueryMapping
    public Mono<Page<Entity<WebFeed>>> feedsSearch(@Arguments SearchFeedsRequest request) {
        PageRequest pageRequest = qsParser.parse(request.toPageRequest());
        Flux<Entity<WebFeed>> feeds = feedService.list(pageRequest);

        return feedService.count(pageRequest)
                .map(count -> Page.of(feeds, count));
    }

    @SchemaMapping(typeName = "SearchFeedsResponse")
    public Flux<Entity<WebFeed>> entities(Page<Entity<WebFeed>> searchFeedsResponse) {
        return Optional.ofNullable(searchFeedsResponse.getBody()).orElse(Flux.empty());
    }

    @SchemaMapping(typeName = "SearchFeedsResponse")
    public Mono<Integer> totalCount(Page<Entity<WebFeed>> searchFeedsResponse) {
        return Mono.justOrEmpty(searchFeedsResponse.getHeaders().get("X-Total-Count"))
                .map(List::getFirst)
                .map(Integer::parseInt)
                .switchIfEmpty(Mono.just(0));
    }

    @BatchMapping
    public Mono<Map<News, List<Entity<WebFeed>>>> feeds(List<News> news) {
        if (news.isEmpty()) {
            return Mono.just(Map.of());
        }
        List<String> feedsIds = news.stream().flatMap(n -> n.getFeeds().stream()).distinct().toList();
        PageRequest pageRequest = qsParser.parse(Map.of(EntitiesProperties.ID, feedsIds));
        return feedService.list(pageRequest).collectList()
                .map(feeds -> feeds.stream().collect(Collectors.toUnmodifiableMap(Entity::id, Function.identity())))
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
        List<String> newsIds = news.stream().map(News::id).toList();
        return popularService.get(newsIds).collect(Collectors.toUnmodifiableMap(Popularity::id, Function.identity()))
                .map(pops -> news.stream()
                        .filter(n -> pops.containsKey(n.id()))
                        .collect(Collectors.toUnmodifiableMap(Function.identity(), n -> pops.get(n.id()))));
    }

    @MutationMapping
    @PreAuthorize("hasAnyRole('USER', 'MANAGER', 'ADMIN')")
    public Mono<Entity<WebFeed>> feedAddAndSubscribe(@Valid @Argument("feed") FeedForm feedForm) {
        if (isNull(feedForm)) {
            return Mono.error(() -> GraphqlErrorException.newErrorException()
                    .errorClassification(ErrorClassification.errorClassification("BAD_REQUEST"))
                    .build());
        }

        URI uri = URI.create(feedForm.location());
        Set<String> tags = Optional.ofNullable(feedForm.tags()).map(Set::copyOf).orElseGet(Set::of);
        var entity = Entity.identify(WebFeed.builder()
                        .location(uri)
                        .tags(tags)
                        .name(feedForm.name())
                        .build())
                .withId(Hasher.identify(uri));
        return feedService.addAndSubscribe(Collections.singleton(entity)).next();
    }

    @MutationMapping
    @PreAuthorize("hasAnyRole('USER', 'MANAGER', 'ADMIN')")
    public Mono<Entity<WebFeed>> subscribe(@Argument String id, @Argument String name, @Argument Collection<String> tags) {
        Set<String> tagsSet = Optional.ofNullable(tags).map(Set::copyOf).orElse(Set.of());
        return feedService.get(id)
                .map(feed -> feed.convert(e -> e.toBuilder().name(name).tags(tagsSet).build()))
                .flatMapMany(f -> feedService.subscribe(Collections.singleton(f)))
                .next();
    }

    @MutationMapping
    @PreAuthorize("hasAnyRole('USER', 'MANAGER', 'ADMIN')")
    public Mono<Entity<WebFeed>> feedUpdate(
            @Argument String id, @Argument String name, @Argument String description, @Argument Collection<String> tags) {
        Set<String> tagsSet = Optional.ofNullable(tags).map(Set::copyOf).orElse(Set.of());
        return feedService.get(id)
                .map(feed -> feed.convert(e -> e.toBuilder().name(name).description(description).tags(tagsSet).build()))
                .flatMapMany(feedService::update)
                .next();
    }
}
