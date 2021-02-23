package fr.ght1pc9kc.baywatch.infra.security;

import fr.ght1pc9kc.baywatch.api.model.BaywatchAuthentication;
import fr.ght1pc9kc.baywatch.domain.ports.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

@Slf4j
@RequiredArgsConstructor
public class JwtTokenAuthenticationFilter implements WebFilter {
    private final JwtTokenProvider tokenProvider;

    @Override
    public @NotNull Mono<Void> filter(ServerWebExchange exchange, @NotNull WebFilterChain chain) {
        return exchange.getSession()
                .flatMap(s -> {
                    String token = s.getAttributeOrDefault("token", resolveHeaderToken(exchange.getRequest()));
                    if (StringUtils.hasText(token) && this.tokenProvider.validateToken(token)) {
                        BaywatchAuthentication bwAuth = this.tokenProvider.getAuthentication(token);
                        return Mono.just(new UsernamePasswordAuthenticationToken(
                                bwAuth.user, bwAuth.token, AuthorityUtils.createAuthorityList(bwAuth.authorities.toArray(String[]::new))
                        ));
                    } else {
                        return Mono.empty();
                    }
                })
                .flatMap(authentication -> chain.filter(exchange).contextWrite(ReactiveSecurityContextHolder.withAuthentication(authentication)))
                .switchIfEmpty(chain.filter(exchange));

//        String token = resolveToken(exchange.getRequest());
//        if (StringUtils.hasText(token) && this.tokenProvider.validateToken(token)) {
//            BaywatchAuthentication bwAuth = this.tokenProvider.getAuthentication(token);
//            UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
//                    bwAuth.user, bwAuth.token, AuthorityUtils.createAuthorityList(bwAuth.authorities.toArray(String[]::new))
//            );
//            return chain.filter(exchange).contextWrite(ReactiveSecurityContextHolder.withAuthentication(authentication));
//        }
//        return chain.filter(exchange);
    }

    private String resolveHeaderToken(ServerHttpRequest request) {
        String bearerToken = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return "";
    }
}
