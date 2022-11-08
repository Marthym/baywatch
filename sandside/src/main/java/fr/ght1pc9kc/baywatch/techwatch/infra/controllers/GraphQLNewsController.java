package fr.ght1pc9kc.baywatch.techwatch.infra.controllers;

import fr.ght1pc9kc.baywatch.common.infra.model.Page;
import fr.ght1pc9kc.baywatch.techwatch.api.ImageProxyService;
import fr.ght1pc9kc.baywatch.techwatch.api.NewsService;
import fr.ght1pc9kc.baywatch.techwatch.api.PopularNewsService;
import fr.ght1pc9kc.baywatch.techwatch.api.model.News;
import fr.ght1pc9kc.baywatch.techwatch.api.model.Popularity;
import fr.ght1pc9kc.baywatch.techwatch.infra.model.graphql.SearchNewsRequest;
import fr.ght1pc9kc.juery.api.PageRequest;
import fr.ght1pc9kc.juery.basic.QueryStringParser;
import lombok.RequiredArgsConstructor;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.Arguments;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.graphql.data.method.annotation.SchemaMapping;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Set;

@Controller
@RequiredArgsConstructor
public class GraphQLNewsController {
    private final NewsService newsService;
    private final PopularNewsService popularService;
    private final ImageProxyService imageProxyService;
    private static final QueryStringParser qsParser = QueryStringParser.withDefaultConfig();

    @QueryMapping
    @PreAuthorize("permitAll()")
    public Mono<Page<News>> newsSearch(@Arguments SearchNewsRequest request) {
        PageRequest pageRequest = qsParser.parse(request.toPageRequest());
        Flux<News> news = newsService.list(pageRequest).map(n -> n.withRaw(n.withImage(imageProxyService.proxify(n.getImage()))));

        return newsService.count(pageRequest)
                .map(count -> Page.of(news, count));
    }

    @SchemaMapping(typeName = "SearchNewsResponse")
    public Flux<News> entities(Page<News> searchNewsResponse) {
        return searchNewsResponse.getBody();
    }

    @SchemaMapping(typeName = "SearchNewsResponse")
    public Mono<Integer> totalCount(Page<News> searchNewsResponse) {
        return Mono.justOrEmpty(searchNewsResponse.getHeaders().get("X-Total-Count"))
                .map(h -> h.get(0))
                .map(Integer::parseInt)
                .switchIfEmpty(Mono.just(0));
    }

    @QueryMapping
    public Flux<Popularity> getNewsPopularity(@Argument("ids") Set<String> newsIds) {
        return popularService.get(newsIds);
    }
}
