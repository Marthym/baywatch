package fr.ght1pc9kc.baywatch.security.infra.adapters;

import fr.ght1pc9kc.baywatch.common.api.LocaleFacade;
import fr.ght1pc9kc.baywatch.security.domain.ports.ClientLocalePort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Locale;

@Component
@RequiredArgsConstructor
public class ClientLocaleAdapter implements ClientLocalePort {
    private final LocaleFacade localeFacade;

    @Override
    public Mono<Locale> getClientLocale() {
        return localeFacade.getLocale();
    }

    @Override
    public List<Locale> getAvailableLanguages() {
        return LocaleFacade.AVAILABLE_LANGUAGES;
    }
}
