package fr.ght1pc9kc.baywatch.notify.domain.ports;

import reactor.core.publisher.Mono;

import java.util.Locale;

public interface NotifyLocaleFacadePort {
    Mono<Locale> getLocale();
}
