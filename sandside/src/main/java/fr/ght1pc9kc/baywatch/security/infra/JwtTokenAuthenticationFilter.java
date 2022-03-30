package fr.ght1pc9kc.baywatch.security.infra;

import fr.ght1pc9kc.baywatch.security.api.model.BaywatchAuthentication;
import fr.ght1pc9kc.baywatch.security.domain.ports.JwtTokenProvider;
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
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.function.Predicate;

@Slf4j
@RequiredArgsConstructor
public class JwtTokenAuthenticationFilter implements WebFilter {
    private final JwtTokenProvider tokenProvider;
    private final TokenCookieManager cookieManager;
    private final ReactiveUserDetailsService userService;

    @Override
    public @NotNull Mono<Void> filter(ServerWebExchange exchange, @NotNull WebFilterChain chain) {
        String token = resolveCookieOrHeader(exchange.getRequest());
        if (StringUtils.hasText(token) && this.tokenProvider.validateToken(token, false)) {
            BaywatchAuthentication bwAuth = this.tokenProvider.getAuthentication(token);
            UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                    bwAuth.user, bwAuth.token, AuthorityUtils.createAuthorityList(bwAuth.authorities.toArray(String[]::new))
            );

            return Mono.just(!this.tokenProvider.validateToken(token, true))
                    .filter(Predicate.isEqual(true))
                    .flatMap(x -> userService.findByUsername(bwAuth.getUser().self.login))
                    .map(updated -> {
                        log.debug("Refresh valid expired token for {}", bwAuth.getUser().self.login);
                        BaywatchAuthentication freshBaywatchAuth = this.tokenProvider.createToken(bwAuth.getUser(), bwAuth.rememberMe, Collections.emptyList());
                        refreshCookieOrHeader(freshBaywatchAuth, exchange.getRequest(), exchange.getResponse());
                        return exchange;
                    }).then(chain.filter(exchange).contextWrite(ReactiveSecurityContextHolder.withAuthentication(authentication)));
        }

        if (StringUtils.hasText(token)) {
            // Remove invalid Token Cookie
            ResponseCookie tokenCookie = cookieManager.buildTokenCookieDeletion(exchange.getRequest().getURI().getScheme());
            exchange.getResponse().addCookie(tokenCookie);
        }
        return chain.filter(exchange);
    }

    private String resolveCookieOrHeader(ServerHttpRequest request) {
        String bearerToken = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return cookieManager.getTokenCookie(request)
                .map(HttpCookie::getValue)
                .filter(StringUtils::hasText)
                .orElse("");
    }

    private void refreshCookieOrHeader(BaywatchAuthentication bwAuth, ServerHttpRequest request, ServerHttpResponse response) {
        String bearerToken = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            response.getHeaders().add(HttpHeaders.AUTHORIZATION, "Bearer " + bwAuth.getToken());
        } else {
            cookieManager.getTokenCookie(request).ifPresent(old ->
                    response.addCookie(cookieManager.buildTokenCookie(request.getURI().getScheme(), bwAuth)));
        }
    }
}
