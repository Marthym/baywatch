package fr.ght1pc9kc.baywatch.security.infra.controllers;

import fr.ght1pc9kc.baywatch.common.api.model.Entity;
import fr.ght1pc9kc.baywatch.common.domain.exceptions.BadRequestCriteria;
import fr.ght1pc9kc.baywatch.common.infra.model.CreateValidation;
import fr.ght1pc9kc.baywatch.common.infra.model.Page;
import fr.ght1pc9kc.baywatch.common.infra.model.PatchOperation;
import fr.ght1pc9kc.baywatch.common.infra.model.PatchPayload;
import fr.ght1pc9kc.baywatch.security.api.UserService;
import fr.ght1pc9kc.baywatch.security.api.model.User;
import fr.ght1pc9kc.baywatch.security.infra.config.SecurityMapper;
import fr.ght1pc9kc.baywatch.security.infra.exceptions.AlreadyExistsException;
import fr.ght1pc9kc.baywatch.security.infra.model.UserForm;
import fr.ght1pc9kc.juery.api.PageRequest;
import fr.ght1pc9kc.juery.basic.QueryStringParser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.support.WebExchangeBindException;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import javax.validation.Valid;
import java.nio.file.Paths;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;
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
    public Mono<Entity<User>> createUser(@Validated({CreateValidation.class}) @RequestBody Mono<UserForm> toCreate) {
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

    @PutMapping("/{id}")
    public Mono<Entity<User>> updateUser(@PathVariable("id") String id, @Valid @RequestBody Mono<UserForm> toUpdate) {
        return toUpdate
                .map(mapper::formToUser)
                .flatMap(u -> userService.update(id, u))
                .onErrorMap(NoSuchElementException.class, e ->
                        new ResponseStatusException(HttpStatus.NOT_FOUND, e.getLocalizedMessage()))
                .onErrorMap(WebExchangeBindException.class, e -> {
                    String message = e.getFieldErrors().stream().map(err -> err.getField() + " " + err.getDefaultMessage()).collect(Collectors.joining("\n"));
                    return new ResponseStatusException(HttpStatus.BAD_REQUEST, message);
                });
    }

    @DeleteMapping("/{id}")
    public Mono<Entity<User>> delete(@PathVariable("id") String id) {
        return userService.delete(List.of(id)).single();
    }

    @PatchMapping
    public Flux<Entity<User>> bulkUpdate(@RequestBody PatchPayload patchs) {
        log.debug(patchs.toString());
        Set<String> ids = patchs.getResources().stream()
                .filter(p -> p.op() == PatchOperation.remove && p.path().getPath().startsWith("/users"))
                .map(p -> Paths.get(p.path()).getFileName().toString())
                .collect(Collectors.toUnmodifiableSet());
        return userService.delete(ids);
    }
}
