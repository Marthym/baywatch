package fr.ght1pc9kc.baywatch.techwatch.infra.controllers;

import fr.ght1pc9kc.baywatch.common.domain.exceptions.BadRequestCriteria;
import fr.ght1pc9kc.baywatch.techwatch.api.FeedService;
import fr.ght1pc9kc.baywatch.techwatch.api.PopularNewsService;
import fr.ght1pc9kc.baywatch.techwatch.api.model.Feed;
import fr.ght1pc9kc.baywatch.techwatch.api.model.Popularity;
import lombok.RequiredArgsConstructor;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Set;

@Controller
@RequiredArgsConstructor
public class GraphQLController {
    private final FeedService feedService;
    private final PopularNewsService popularService;

    @QueryMapping
    public Mono<Feed> getFeed(@Argument("id") String id) {
        return feedService.get(id)
                .onErrorMap(BadRequestCriteria.class, e -> new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getLocalizedMessage()));
    }

    @QueryMapping
    public Flux<Popularity> getNewsPopularity(@Argument("ids") Set<String> newsIds) {
        return popularService.get(newsIds)
                .onErrorMap(BadRequestCriteria.class, e -> new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getLocalizedMessage()));
    }
}
