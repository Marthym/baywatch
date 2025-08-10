package fr.ght1pc9kc.baywatch.common.api;

import reactor.core.publisher.Mono;

import java.net.InetSocketAddress;
import java.net.URI;
import java.util.List;
import java.util.Locale;

public interface ClientInfoFacade {
    List<Locale> AVAILABLE_LANGUAGES = List.of(Locale.US, Locale.FRANCE);

    Mono<Locale> getLocale();

    Mono<String> getUserAgent();

    Mono<InetSocketAddress> getRemoteAddress();

    Mono<URI> getBaseUrl();
}
