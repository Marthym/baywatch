package fr.ght1pc9kc.baywatch.common.api;

import reactor.core.publisher.Mono;

import java.util.Locale;

public interface LocaleFacade {
    Mono<Locale> getLocale();
}
