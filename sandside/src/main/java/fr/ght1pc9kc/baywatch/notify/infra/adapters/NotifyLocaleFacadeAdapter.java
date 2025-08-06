package fr.ght1pc9kc.baywatch.notify.infra.adapters;

import fr.ght1pc9kc.baywatch.common.api.LocaleFacade;
import fr.ght1pc9kc.baywatch.notify.domain.ports.NotifyLocaleFacadePort;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Delegate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class NotifyLocaleFacadeAdapter implements NotifyLocaleFacadePort {
    @Delegate
    private final LocaleFacade localeFacade;
}
