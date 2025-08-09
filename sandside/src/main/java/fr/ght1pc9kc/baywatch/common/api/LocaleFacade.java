package fr.ght1pc9kc.baywatch.common.api;

import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Locale;

public interface LocaleFacade {
    List<Locale> AVAILABLE_LANGUAGES = List.of(Locale.US, Locale.FRANCE);

    Mono<Locale> getLocale();
}
