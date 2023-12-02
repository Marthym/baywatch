package fr.ght1pc9kc.baywatch.common.infra.filters;

import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

@Component
public class LocaleContextWebFilter implements WebFilter {
    @Override
    public @NotNull Mono<Void> filter(@NotNull ServerWebExchange exchange, WebFilterChain chain) {
        return chain.filter(exchange).contextWrite(ReactiveLocaleContextHolder.withLocaleContext(Mono.just(exchange.getLocaleContext())));
    }
}
