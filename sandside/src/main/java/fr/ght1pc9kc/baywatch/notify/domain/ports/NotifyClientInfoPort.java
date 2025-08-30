package fr.ght1pc9kc.baywatch.notify.domain.ports;

import reactor.core.publisher.Mono;

import java.net.InetSocketAddress;
import java.net.URI;
import java.util.List;
import java.util.Locale;

public interface NotifyClientInfoPort {
    Mono<Locale> getLocale();

    List<Locale> getAvailableLanguages();

    Mono<InetSocketAddress> getRemoteAddress();

    Mono<URI> getBaseUrl();
}
