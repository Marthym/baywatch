package fr.ght1pc9kc.baywatch.infra.security;

import fr.ght1pc9kc.baywatch.api.UserService;
import fr.ght1pc9kc.baywatch.api.security.model.BaywatchAuthentication;
import fr.ght1pc9kc.baywatch.domain.ports.JwtTokenProvider;
import fr.ght1pc9kc.baywatch.infra.security.model.SecurityParams;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.http.HttpCookie;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.Optional;
import java.util.function.Predicate;

@Slf4j
@RequiredArgsConstructor
public class JwtTokenAuthenticationFilter implements WebFilter {
    private final JwtTokenProvider tokenProvider;
    private final SecurityParams securityParams;
    private final UserService userService;

    @Override
    public @NotNull Mono<Void> filter(ServerWebExchange exchange, @NotNull WebFilterChain chain) {
        String token = resolveToken(exchange.getRequest());
        if (StringUtils.hasText(token) && this.tokenProvider.validateToken(token, false)) {
            BaywatchAuthentication bwAuth = this.tokenProvider.getAuthentication(token);
            UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                    bwAuth.user, bwAuth.token, AuthorityUtils.createAuthorityList(bwAuth.authorities.toArray(String[]::new))
            );

            return Mono.just(!this.tokenProvider.validateToken(token, true))
                    .filter(Predicate.isEqual(true))
                    .flatMap(x -> userService.get(bwAuth.getUser().id))
                    .map(updated -> {
                        log.debug("Refresh valid expired token for {}", bwAuth.getUser().login);
                        String t = this.tokenProvider.createToken(bwAuth.getUser(), Collections.emptyList());
                        refreshToken(t, exchange.getRequest(), exchange.getResponse());
                        return t;
                    }).then(chain.filter(exchange).contextWrite(ReactiveSecurityContextHolder.withAuthentication(authentication)));

//            return chain.filter(exchange).contextWrite(ReactiveSecurityContextHolder.withAuthentication(authentication));
        }

        if (StringUtils.hasText(token)) {
            // Remove invalid Token Cookie
            exchange.getResponse().addCookie(
                    ResponseCookie.from(securityParams.cookie.name, "")
                            .httpOnly(true)
                            .secure("https".equals(exchange.getRequest().getURI().getScheme()))
                            .sameSite("Strict")
                            .path("/api")
                            .maxAge(0)
                            .build());
        }
        return chain.filter(exchange);
    }

    private String resolveToken(ServerHttpRequest request) {
        String bearerToken = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return Optional.ofNullable(request.getCookies().getFirst(securityParams.cookie.name))
                .map(HttpCookie::getValue)
                .filter(StringUtils::hasText)
                .orElse("");
    }

    private void refreshToken(String token, ServerHttpRequest request, ServerHttpResponse response) {
        String bearerToken = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            response.getHeaders().add(HttpHeaders.AUTHORIZATION, "Bearer " + token);
        } else {
            //TODO: Mettre tous les paramètres du cookie
            //TODO: Le cookie doit avoir un délai expiration spécifique
            Optional.ofNullable(request.getCookies().getFirst(securityParams.cookie.name))
                    .ifPresent(old -> response.addCookie(
                            ResponseCookie.from(securityParams.cookie.name, token)
                                    .httpOnly(true)
                                    .sameSite("Strict")
                                    .build()));
        }
    }
}
