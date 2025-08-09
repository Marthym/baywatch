package fr.ght1pc9kc.baywatch.common.infra.adapters;

import fr.ght1pc9kc.baywatch.common.api.ClientInfoFacade;
import fr.ght1pc9kc.baywatch.common.api.model.ClientInfoContext;
import fr.ght1pc9kc.baywatch.common.infra.filters.ReactiveClientInfoContextHolder;
import fr.ght1pc9kc.baywatch.common.infra.filters.ReactiveLocaleContextHolder;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.net.InetSocketAddress;
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
        });
    }

    @Override
    public Mono<String> getUserAgent() {
        return ReactiveClientInfoContextHolder.getContext()
                .map(ClientInfoContext::userAgent);
    }

    @Override
    public Mono<InetSocketAddress> getRemoteAddress() {
        return ReactiveClientInfoContextHolder.getContext()
                .map(ClientInfoContext::ip);
    }
}
