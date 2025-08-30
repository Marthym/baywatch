package fr.ght1pc9kc.baywatch.common.infra.adapters;

import fr.ght1pc9kc.baywatch.common.api.ClientInfoFacade;
import fr.ght1pc9kc.baywatch.common.api.model.ClientInfoContext;
import fr.ght1pc9kc.baywatch.common.infra.filters.ReactiveClientInfoContextHolder;
import fr.ght1pc9kc.baywatch.common.infra.filters.ReactiveLocaleContextHolder;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.net.InetSocketAddress;
import java.net.URI;
import java.util.Locale;

@Service
public record ClientInfoFacadeAdapter() implements ClientInfoFacade {

    @Override
    public Mono<Locale> getLocale() {
        return ReactiveLocaleContextHolder.getContext().map(localeCtx -> {
            Locale locale = localeCtx.getLocale();
            if (locale != null) {
                return locale;
            } else {
                return Locale.getDefault();
            }
        }).switchIfEmpty(Mono.just(Locale.getDefault()));
    }

    @Override
    public Mono<String> getUserAgent() {
        return ReactiveClientInfoContextHolder.getContext()
                .map(ClientInfoContext::userAgent)
                .switchIfEmpty(Mono.error(() -> new IllegalStateException("No user agent found in context")));
    }

    @Override
    public Mono<InetSocketAddress> getRemoteAddress() {
        return ReactiveClientInfoContextHolder.getContext()
                .map(ClientInfoContext::ip)
                .switchIfEmpty(Mono.error(() -> new IllegalStateException("No remote IP found in context")));
    }

    @Override
    public Mono<URI> getBaseUrl() {
        return ReactiveClientInfoContextHolder.getContext()
                .map(ClientInfoContext::baseUrl)
                .switchIfEmpty(Mono.error(() -> new IllegalStateException("No base URL found in context")));
    }
}
