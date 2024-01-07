package fr.ght1pc9kc.baywatch.security.infra.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.MapType;
import fr.ght1pc9kc.baywatch.common.api.model.Entity;
import fr.ght1pc9kc.baywatch.common.domain.exceptions.BadRequestCriteria;
import fr.ght1pc9kc.baywatch.common.infra.model.CreateValidation;
import fr.ght1pc9kc.baywatch.common.infra.model.Page;
import fr.ght1pc9kc.baywatch.security.api.AuthorizationService;
import fr.ght1pc9kc.baywatch.security.api.UserService;
import fr.ght1pc9kc.baywatch.security.api.model.Permission;
import fr.ght1pc9kc.baywatch.security.api.model.User;
import fr.ght1pc9kc.baywatch.security.infra.adapters.UserMapper;
import fr.ght1pc9kc.baywatch.security.infra.exceptions.AlreadyExistsException;
import fr.ght1pc9kc.baywatch.security.infra.model.UserForm;
import fr.ght1pc9kc.baywatch.security.infra.model.UserSearchRequest;
import fr.ght1pc9kc.juery.api.PageRequest;
import fr.ght1pc9kc.juery.basic.QueryStringParser;
import graphql.GraphQLError;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.Arguments;
import org.springframework.graphql.data.method.annotation.GraphQlExceptionHandler;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.graphql.data.method.annotation.SchemaMapping;
import org.springframework.graphql.execution.ErrorType;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.support.WebExchangeBindException;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Controller
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class UserGqlController {
    private static final QueryStringParser qsParser = QueryStringParser.withDefaultConfig();
    private final UserService userService;
    private final AuthorizationService authorizationService;
    private final ObjectMapper mapper;
    private final UserMapper userMapper;

    @QueryMapping
    @PreAuthorize("isAuthenticated()")
    public Mono<Page<Entity<User>>> userSearch(@Arguments UserSearchRequest request) {
        PageRequest pageRequest = qsParser.parse(request.toPageRequest());
        Flux<Entity<User>> users = userService.list(pageRequest)
                .onErrorMap(BadRequestCriteria.class, e -> new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getLocalizedMessage()));

        return userService.count(pageRequest)
                .map(count -> Page.of(users, count));
    }

    @MutationMapping
    @PreAuthorize("isAuthenticated()")
    public Mono<Map<String, Object>> userCreate(@Validated({CreateValidation.class}) @Argument("user") UserForm user) {
        MapType gqlType = mapper.getTypeFactory().constructMapType(Map.class, String.class, Object.class);

        return userService.create(mapper.convertValue(user, User.class))
                .onErrorMap(AlreadyExistsException.class, e ->
                        new ResponseStatusException(HttpStatus.CONFLICT, e.getLocalizedMessage()))
                .onErrorMap(WebExchangeBindException.class, e -> {
                    String message = e.getFieldErrors().stream().map(err ->
                            err.getField() + " " + err.getDefaultMessage()).collect(Collectors.joining("\n"));
                    return new ResponseStatusException(HttpStatus.BAD_REQUEST, message);
                })
                .map(e -> mapper.convertValue(e, gqlType));
    }

    @MutationMapping
    @PreAuthorize("isAuthenticated()")
    public Mono<Map<String, Object>> userUpdate(
            @Argument("_id") String id,
            @Argument("currentPassword") String currentPassword, @Valid @Argument("user") Map<String, Object> toUpdate) {
        MapType gqlType = mapper.getTypeFactory().constructMapType(Map.class, String.class, Object.class);

        return Mono.fromCallable(() -> userMapper.getUpdatableUser(toUpdate))
                .flatMap(user -> userService.update(id, user, currentPassword))
                .map(e -> mapper.convertValue(e, gqlType));
    }

    @GraphQlExceptionHandler
    public Mono<GraphQLError> handle(NoSuchElementException ex) {
        return Mono.just(GraphQLError.newError()
                .errorType(ErrorType.NOT_FOUND)
                .message(ex.getLocalizedMessage())
                .build());
    }

    @GraphQlExceptionHandler
    public Mono<GraphQLError> handle(IllegalArgumentException ex) {
        return Mono.just(GraphQLError.newError()
                .errorType(ErrorType.BAD_REQUEST)
                .message(ex.getLocalizedMessage())
                .build());
    }

    @MutationMapping
    public Mono<Map<String, Object>> userGrants(@Argument("_id") String id, @Argument("permissions") Collection<String> permString) {
        MapType gqlType = mapper.getTypeFactory().constructMapType(Map.class, String.class, Object.class);

        List<Permission> permissions = permString.stream().map(Permission::from).distinct().toList();
        return authorizationService.grants(id, permissions).map(e -> mapper.convertValue(e, gqlType));
    }

    @MutationMapping
    public Flux<Map<String, Object>> userDelete(@Argument("ids") Collection<String> ids) {
        if (Objects.isNull(ids) || ids.isEmpty()) {
            return Flux.empty();
        }
        MapType gqlType = mapper.getTypeFactory().constructMapType(Map.class, String.class, Object.class);
        return userService.delete(List.copyOf(ids))
                .map(e -> mapper.convertValue(e, gqlType));
    }

    @PreAuthorize("isAuthenticated()")
    @SchemaMapping(typeName = "SearchUsersResponse")
    public Flux<Map<String, Object>> entities(Page<Entity<User>> searchNewsResponse) {
        MapType gqlType = mapper.getTypeFactory().constructMapType(Map.class, String.class, Object.class);
        return Optional.ofNullable(searchNewsResponse.getBody()).orElse(Flux.empty())
                .map(e -> mapper.convertValue(e, gqlType));
    }

    @PreAuthorize("isAuthenticated()")
    @SchemaMapping(typeName = "SearchUsersResponse")
    public Mono<Integer> totalCount(Page<Entity<User>> searchNewsResponse) {
        return Mono.justOrEmpty(searchNewsResponse.getHeaders().get("X-Total-Count"))
                .map(h -> h.get(0))
                .map(Integer::parseInt)
                .switchIfEmpty(Mono.just(0));
    }
}
