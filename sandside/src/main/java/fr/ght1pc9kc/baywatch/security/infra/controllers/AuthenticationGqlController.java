package fr.ght1pc9kc.baywatch.security.infra.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import fr.ght1pc9kc.baywatch.security.api.AuthenticationFacade;
import fr.ght1pc9kc.baywatch.security.api.model.BaywatchAuthentication;
import fr.ght1pc9kc.baywatch.security.domain.exceptions.SecurityException;
import fr.ght1pc9kc.baywatch.security.domain.ports.JwtTokenProvider;
import fr.ght1pc9kc.baywatch.security.infra.TokenCookieManager;
import fr.ght1pc9kc.baywatch.security.infra.adapters.AuthenticationManagerAdapter;
import fr.ght1pc9kc.baywatch.security.infra.exceptions.BaywatchCredentialsException;
import fr.ght1pc9kc.baywatch.security.infra.model.AuthenticationRequest;
import fr.ght1pc9kc.baywatch.security.infra.model.BaywatchUserDetails;
import fr.ght1pc9kc.baywatch.security.infra.model.Session;
import graphql.GraphQLContext;
import graphql.GraphqlErrorException;
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
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuples;

import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Set;

@Slf4j
@Controller
@RequiredArgsConstructor
public class AuthenticationGqlController {

    private final JwtTokenProvider tokenProvider;
    private final AuthenticationManagerAdapter authenticationManager;
    private final AuthenticationFacade authFacade;
    private final TokenCookieManager cookieManager;
    private final ObjectMapper jsonMapper;

    @MutationMapping
    @PreAuthorize("permitAll()")
    public Mono<Object> login(@Arguments AuthenticationRequest authRequest, GraphQLContext env) {
        return Mono.just(authRequest)
                .flatMap(login -> authenticationManager.authenticate(
                                new UsernamePasswordAuthenticationToken(login.getUsername(), login.getPassword()))
                        .map(u -> Tuples.of(login.rememberMe(), u)))

                .map(auth -> {
                    BaywatchUserDetails user = (BaywatchUserDetails) auth.getT2().getPrincipal();

                    Optional<ServerWebExchange> orEmpty = env.getOrEmpty(ServerWebExchange.class);
                    orEmpty.ifPresent(exchange -> {
                        Set<String> authorities = AuthorityUtils.authorityListToSet(auth.getT2().getAuthorities());
                        BaywatchAuthentication bwAuth = tokenProvider.createToken(user.getEntity(), auth.getT1(), authorities);
                        ResponseCookie authCookie = cookieManager.buildTokenCookie(exchange.getRequest().getURI().getScheme(), bwAuth);
                        exchange.getResponse().addCookie(authCookie);
                    });

                    log.debug("Login to {}.", user.getUsername());
                    return jsonMapper.convertValue(user.getEntity(), Object.class);
                })

                .onErrorMap(BadCredentialsException.class, BaywatchCredentialsException::new)
                .onErrorMap(NoSuchElementException.class, BaywatchCredentialsException::new);
    }

    @MutationMapping
    @PreAuthorize("isAuthenticated()")
    public Mono<Void> logout(GraphQLContext env) {
        return Mono.fromRunnable(() -> env.<ServerWebExchange>getOrEmpty(ServerWebExchange.class).ifPresent(exchange -> {
            if (log.isDebugEnabled()) {
                String user = cookieManager.getTokenCookie(exchange.getRequest())
                        .map(HttpCookie::getValue)
                        .map(tokenProvider::getAuthentication)
                        .map(a -> String.format("%s (%s)", a.user.self.login, a.user.id))
                        .orElse("Unknown User");
                log.debug("Logout for {}.", user);
            }
            exchange.getResponse().addCookie(
                    cookieManager.buildTokenCookieDeletion(exchange.getRequest().getURI().getScheme()));
        }));
    }

    @MutationMapping
    @PreAuthorize("permitAll()")
    public Mono<Session> refreshSession(GraphQLContext env) {
        return env.<ServerWebExchange>getOrEmpty(ServerWebExchange.class).flatMap(exchange ->
                        cookieManager.getTokenCookie(exchange.getRequest())
                                .map(HttpCookie::getValue)
                                .map(authenticationManager::refresh)
                                .map(mauth -> mauth.map(auth -> {
                                    ResponseCookie tokenCookie = cookieManager.buildTokenCookie(exchange.getRequest().getURI().getScheme(), auth);
                                    exchange.getResponse().addCookie(tokenCookie);
                                    return Session.builder()
                                            .user(auth.user.self)
                                            .maxAge(-1)
                                            .build();
                                }).onErrorMap(SecurityException.class, e -> GraphqlErrorException.newErrorException()
                                        .message("User token has expired !")
                                        .errorClassification(ErrorType.UNAUTHORIZED)
                                        .cause(e)
                                        .build())))
                .orElseGet(() -> Mono.error(GraphqlErrorException.newErrorException()
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
