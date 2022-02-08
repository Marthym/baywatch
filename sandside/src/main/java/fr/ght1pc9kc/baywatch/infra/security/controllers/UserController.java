package fr.ght1pc9kc.baywatch.infra.security.controllers;

import fr.ght1pc9kc.baywatch.api.common.model.Entity;
import fr.ght1pc9kc.baywatch.api.security.UserService;
import fr.ght1pc9kc.baywatch.api.security.model.User;
import fr.ght1pc9kc.baywatch.domain.exceptions.BadRequestCriteria;
import fr.ght1pc9kc.baywatch.infra.common.model.Page;
import fr.ght1pc9kc.baywatch.infra.security.config.SecurityMapper;
import fr.ght1pc9kc.baywatch.infra.security.exceptions.AlreadyExistsException;
import fr.ght1pc9kc.baywatch.infra.security.model.UserForm;
import fr.ght1pc9kc.juery.api.PageRequest;
import fr.ght1pc9kc.juery.basic.QueryStringParser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.support.WebExchangeBindException;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import javax.validation.Valid;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@RequestMapping("${baywatch.base-route}/users")
public class UserController {
    private static final QueryStringParser qsParser = QueryStringParser.withDefaultConfig();
    private final UserService userService;
    private final SecurityMapper mapper;

    @GetMapping("/{id}")
    public Mono<Entity<User>> get(@PathVariable("id") String id) {
        return userService.get(id)
                .onErrorMap(BadRequestCriteria.class, e -> new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getLocalizedMessage()));
    }

    @GetMapping
    public Mono<ResponseEntity<Flux<Entity<User>>>> list(ServerHttpRequest request) {
        PageRequest pageRequest = qsParser.parse(request.getQueryParams());
        Flux<Entity<User>> users = userService.list(pageRequest)
                .onErrorMap(BadRequestCriteria.class, e -> new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getLocalizedMessage()));

        return userService.count(pageRequest)
                .map(count -> Page.of(users, count));
    }

    @PostMapping
    public Mono<Entity<User>> createUser(@Valid @RequestBody Mono<UserForm> toCreate) {
        return toCreate
                .map(mapper::formToUser)
                .flatMap(userService::create)
                .onErrorMap(AlreadyExistsException.class, e ->
                        new ResponseStatusException(HttpStatus.CONFLICT, e.getLocalizedMessage()))
                .onErrorMap(WebExchangeBindException.class, e -> {
                    String message = e.getFieldErrors().stream().map(err -> err.getField() + " " + err.getDefaultMessage()).collect(Collectors.joining("\n"));
                    return new ResponseStatusException(HttpStatus.BAD_REQUEST, message);
                });
    }
}
