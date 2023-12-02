package fr.ght1pc9kc.baywatch.common.infra.adapters;

import fr.ght1pc9kc.baywatch.common.api.LocaleFacade;
import fr.ght1pc9kc.baywatch.common.infra.filters.ReactiveLocaleContextHolder;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.Locale;

@Service
public record LocaleFacadeAdapter() implements LocaleFacade {

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
}
