package fr.ght1pc9kc.baywatch.common.infra.filters;

import org.jetbrains.annotations.NotNull;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class ClientInfoContextWebFilter implements WebFilter {
    @Override
    public @NotNull Mono<Void> filter(@NotNull ServerWebExchange exchange, WebFilterChain chain) {
        return chain.filter(exchange).contextWrite(ReactiveClientInfoContextHolder.withClientInfo(
                exchange.getRequest().getRemoteAddress(),
                exchange.getRequest().getHeaders().getFirst(HttpHeaders.USER_AGENT))
        );
    }
}
