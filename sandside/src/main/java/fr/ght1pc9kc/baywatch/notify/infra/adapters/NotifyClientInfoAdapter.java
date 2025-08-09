package fr.ght1pc9kc.baywatch.notify.infra.adapters;

import fr.ght1pc9kc.baywatch.common.api.ClientInfoFacade;
import fr.ght1pc9kc.baywatch.notify.domain.ports.NotifyClientInfoPort;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Delegate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Locale;

@Component
@RequiredArgsConstructor
public class NotifyClientInfoAdapter implements NotifyClientInfoPort {
    @Delegate
    private final ClientInfoFacade clientInfoFacade;

    @Override
    public List<Locale> getAvailableLanguages() {
        return ClientInfoFacade.AVAILABLE_LANGUAGES;
    }
}
