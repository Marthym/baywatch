package fr.ght1pc9kc.baywatch.security.domain.ports;

import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Locale;

public interface ClientLocalePort {
    Mono<Locale> getClientLocale();

    List<Locale> getAvailableLanguages();
}
