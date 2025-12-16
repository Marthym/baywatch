package fr.ght1pc9kc.baywatch.common.infra.filters;

import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

/**
 * A reactive web filter that manages the locale context for the current request
 * by integrating LocaleContext information into the Reactor context.
 * <p>
 * This filter extracts the {@link org.springframework.context.i18n.LocaleContext}
 * from the {@link ServerWebExchange},
 * then stores it in the reactive context using {@link ReactiveLocaleContextHolder#withLocaleContext(Mono)}.
 * This allows downstream components to access the locale information in a reactive, non-blocking way.
 */
@Component
public class LocaleContextWebFilter implements WebFilter {
    @Override
    public @NotNull Mono<Void> filter(@NotNull ServerWebExchange exchange, WebFilterChain chain) {
        return chain.filter(exchange)
                .contextWrite(ReactiveLocaleContextHolder.withLocaleContext(Mono.just(exchange.getLocaleContext())));
    }
}
