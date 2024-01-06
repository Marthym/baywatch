package fr.ght1pc9kc.baywatch.security.infra.adapters;

import fr.ght1pc9kc.baywatch.notify.api.NotifyService;
import fr.ght1pc9kc.baywatch.security.domain.ports.NotificationPort;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Delegate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class NotificationServiceAdapter implements NotificationPort {
    @Delegate
    private final NotifyService delegate;
}
