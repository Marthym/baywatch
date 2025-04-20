package fr.ght1pc9kc.baywatch.admin.infra.controllers;

import fr.ght1pc9kc.baywatch.admin.api.FeedManagementService;
import fr.ght1pc9kc.baywatch.admin.api.model.RawFeed;
import fr.ght1pc9kc.baywatch.admin.infra.mappers.RawFeedMapper;
import fr.ght1pc9kc.baywatch.admin.infra.model.AdminRawFeedForm;
import fr.ght1pc9kc.baywatch.admin.infra.model.AdminRawFeedRequest;
import fr.ght1pc9kc.baywatch.common.infra.model.Page;
import fr.ght1pc9kc.entity.api.Entity;
import fr.ght1pc9kc.juery.api.Criteria;
import fr.ght1pc9kc.juery.api.PageRequest;
import fr.ght1pc9kc.juery.basic.QueryStringParser;
import graphql.GraphQLError;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.Arguments;
import org.springframework.graphql.data.method.annotation.GraphQlExceptionHandler;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.graphql.data.method.annotation.SchemaMapping;
import org.springframework.graphql.execution.ErrorType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Collection;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

@Slf4j
@Controller
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class FeedManagementController {
    private static final QueryStringParser QUERY_STRING_PARSER = QueryStringParser.withDefaultConfig();

    private final FeedManagementService feedManagementService;
    private final RawFeedMapper mapper;

    @QueryMapping
    public Mono<Page<Entity<RawFeed>>> adminRawFeedFind(@Arguments AdminRawFeedRequest request) {
        PageRequest extServerPageRequest = QUERY_STRING_PARSER.parse(request.toPageRequest());
        var entities = feedManagementService.find(extServerPageRequest);
        return feedManagementService.count(extServerPageRequest)
                .map(count -> Page.of(entities, count));
    }

    @QueryMapping
    public Mono<Entity<RawFeed>> adminRawFeedGet(@Argument("_id") String id) {
        return feedManagementService.find(PageRequest.one(Criteria.property("_id").eq(id))).next();
    }

    @MutationMapping
    public Mono<Entity<RawFeed>> adminRawFeedUpdate(
            @Argument("_id") String id, @Argument("extServer") AdminRawFeedForm extServer) {
        return feedManagementService.update(id, mapper.toRawFeed(extServer));
    }

    @MutationMapping
    public Mono<Entity<RawFeed>> adminRawFeedCreate(@Argument("extServer") AdminRawFeedForm extServer) {
        return feedManagementService.create(mapper.toRawFeed(extServer));
    }

    @MutationMapping
    public Mono<Void> adminRawFeedDelete(@Argument("_id") Collection<String> ids) {
        return feedManagementService.delete(ids);
    }

    @SchemaMapping(typeName = "SearchRawFeedsResponse")
    public Flux<Entity<RawFeed>> entities(Page<Entity<RawFeed>> searchRawFeedsResponse) {
        return Optional.ofNullable(searchRawFeedsResponse.getBody()).orElse(Flux.empty());
    }

    @SchemaMapping(typeName = "SearchRawFeedsResponse")
    public Mono<Integer> totalCount(Page<Entity<RawFeed>> searchRawFeedsResponse) {
        return Mono.justOrEmpty(searchRawFeedsResponse.getHeaders().get("X-Total-Count"))
                .map(List::getFirst)
                .map(Integer::parseInt)
                .switchIfEmpty(Mono.just(0));
    }

    @GraphQlExceptionHandler
    @SuppressWarnings("DuplicatedCode")
    public GraphQLError handle(Exception ex) {
        return switch (ex) {
            case NullPointerException npe -> GraphQLError.newError()
                    .errorType(ErrorType.BAD_REQUEST)
                    .message(npe.getLocalizedMessage())
                    .build();
            case NoSuchElementException nse -> GraphQLError.newError()
                    .errorType(ErrorType.NOT_FOUND)
                    .message(nse.getLocalizedMessage())
                    .build();
            default -> {
                log.atError().addArgument(ex.getClass()).addArgument(ex.getLocalizedMessage())
                        .log("{}: {}");
                log.atDebug().log("STACKTRACE", ex);
                yield GraphQLError.newError()
                        .errorType(ErrorType.INTERNAL_ERROR).message(ex.getLocalizedMessage())
                        .build();
            }
        };
    }

}
