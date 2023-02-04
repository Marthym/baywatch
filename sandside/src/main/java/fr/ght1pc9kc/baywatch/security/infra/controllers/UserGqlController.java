package fr.ght1pc9kc.baywatch.security.infra.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.MapType;
import fr.ght1pc9kc.baywatch.common.api.model.Entity;
import fr.ght1pc9kc.baywatch.common.domain.exceptions.BadRequestCriteria;
import fr.ght1pc9kc.baywatch.common.infra.model.Page;
import fr.ght1pc9kc.baywatch.security.api.UserService;
import fr.ght1pc9kc.baywatch.security.api.model.User;
import fr.ght1pc9kc.baywatch.security.infra.model.UserSearchRequest;
import fr.ght1pc9kc.juery.api.PageRequest;
import fr.ght1pc9kc.juery.basic.QueryStringParser;
import lombok.RequiredArgsConstructor;
import org.springframework.graphql.data.method.annotation.Arguments;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.graphql.data.method.annotation.SchemaMapping;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Map;
import java.util.Optional;

@Controller
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class UserGqlController {
    private static final QueryStringParser qsParser = QueryStringParser.withDefaultConfig();
    private final UserService userService;
    private final ObjectMapper mapper;

    @QueryMapping
    public Mono<Page<Entity<User>>> userSearch(@Arguments UserSearchRequest request) {
        PageRequest pageRequest = qsParser.parse(request.toPageRequest());
        Flux<Entity<User>> users = userService.list(pageRequest)
                .onErrorMap(BadRequestCriteria.class, e -> new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getLocalizedMessage()));

        return userService.count(pageRequest)
                .map(count -> Page.of(users, count));
    }

    @SchemaMapping(typeName = "SearchUsersResponse")
    public Flux<Map<String, Object>> entities(Page<Entity<User>> searchNewsResponse) {
        MapType gqlType = mapper.getTypeFactory().constructMapType(Map.class, String.class, Object.class);
        return Optional.ofNullable(searchNewsResponse.getBody()).orElse(Flux.empty())
                .map(e -> mapper.convertValue(e, gqlType));
    }

    @SchemaMapping(typeName = "SearchUsersResponse")
    public Mono<Integer> totalCount(Page<Entity<User>> searchNewsResponse) {
        return Mono.justOrEmpty(searchNewsResponse.getHeaders().get("X-Total-Count"))
                .map(h -> h.get(0))
                .map(Integer::parseInt)
                .switchIfEmpty(Mono.just(0));
    }
}
