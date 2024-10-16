package fr.ght1pc9kc.baywatch.security.infra.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import fr.ght1pc9kc.baywatch.security.api.AuthenticationFacade;
import fr.ght1pc9kc.baywatch.security.api.AuthenticationService;
import fr.ght1pc9kc.baywatch.security.api.model.AuthenticationRequest;
import fr.ght1pc9kc.baywatch.security.domain.exceptions.SecurityException;
import fr.ght1pc9kc.baywatch.security.domain.ports.JwtTokenProvider;
import fr.ght1pc9kc.baywatch.security.infra.TokenCookieManager;
import fr.ght1pc9kc.baywatch.security.infra.exceptions.BaywatchCredentialsException;
import fr.ght1pc9kc.baywatch.security.infra.model.Session;
import graphql.GraphQLContext;
import graphql.GraphqlErrorException;
import io.micrometer.observation.ObservationRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.graphql.data.method.annotation.Arguments;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.graphql.execution.ErrorType;
import org.springframework.http.HttpCookie;
import org.springframework.http.ResponseCookie;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Controller;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.observability.micrometer.Micrometer;
import reactor.core.publisher.Mono;

import java.util.NoSuchElementException;
import java.util.Optional;

@Slf4j
@Controller
@RequiredArgsConstructor
public class AuthenticationGqlController {

    private final JwtTokenProvider tokenProvider;
    private final AuthenticationService authenticationService;
    private final AuthenticationFacade authFacade;
    private final TokenCookieManager cookieManager;
    private final ObjectMapper jsonMapper;
    private final ObservationRegistry registry;

    @MutationMapping
    @PreAuthorize("permitAll()")
    public Mono<Object> login(@Arguments AuthenticationRequest authRequest, GraphQLContext env) {
        return authenticationService.login(authRequest).map(bwAuth -> {
                    Optional<ServerWebExchange> orEmpty = env.getOrEmpty(ServerWebExchange.class);
                    orEmpty.ifPresent(exchange -> {
                        ResponseCookie authCookie = cookieManager.buildTokenCookie(exchange.getRequest().getURI().getScheme(), bwAuth);
                        exchange.getResponse().addCookie(authCookie);
                    });
                    return jsonMapper.convertValue(bwAuth.user(), Object.class);
                })

                .onErrorMap(BadCredentialsException.class, BaywatchCredentialsException::new)
                .onErrorMap(NoSuchElementException.class, BaywatchCredentialsException::new)

                .name("bw.login")
                .tag("username", authRequest.username())
                .tap(Micrometer.observation(registry))
                ;
    }

    @MutationMapping
    @PreAuthorize("isAuthenticated()")
    public Mono<Void> logout(GraphQLContext env) {
        return Mono.fromRunnable(() -> env.<ServerWebExchange>getOrEmpty(ServerWebExchange.class).ifPresent(exchange -> {
            if (log.isDebugEnabled()) {
                String user = cookieManager.getTokenCookie(exchange.getRequest())
                        .map(HttpCookie::getValue)
                        .map(tokenProvider::getAuthentication)
                        .map(a -> String.format("%s (%s)", a.user().self().login(), a.user().id()))
                        .orElse("Unknown User");
                log.debug("Logout for {}.", user);
            }
            cookieManager.buildTokenCookieDeletion(exchange.getRequest().getURI().getScheme()).forEach(ct ->
                    exchange.getResponse().addCookie(ct));
        }));
    }

    @MutationMapping
    @PreAuthorize("permitAll()")
    public Mono<Object> refreshSession(GraphQLContext env) {
        return env.<ServerWebExchange>getOrEmpty(ServerWebExchange.class)
                .flatMap(exchange ->
                        cookieManager.getTokenCookie(exchange.getRequest())
                                .map(HttpCookie::getValue)
                                .map(authenticationService::refresh)
                                .map(mauth -> mauth.map(auth -> {
                                            ResponseCookie tokenCookie = cookieManager.buildTokenCookie(exchange.getRequest().getURI().getScheme(), auth);
                                            exchange.getResponse().addCookie(tokenCookie);
                                            return Session.builder()
                                                    .user(auth.user())
                                                    .maxAge(-1)
                                                    .build();
                                        }).map(s -> jsonMapper.convertValue(s, Object.class)
                                        ).onErrorMap(SecurityException.class, e -> GraphqlErrorException.newErrorException()
                                                .message("User token has expired !")
                                                .errorClassification(ErrorType.UNAUTHORIZED)
                                                .cause(e)
                                                .build())
                                )
                ).orElseGet(() -> Mono.error(GraphqlErrorException.newErrorException()
                        .message("User is not logged on !")
                        .errorClassification(ErrorType.NOT_FOUND)
                        .build()));
    }

    @QueryMapping
    @PreAuthorize("isAuthenticated()")
    public Mono<Object> currentUser() {
        return authFacade.getConnectedUser().map(u -> jsonMapper.convertValue(u, Object.class));
    }
}
