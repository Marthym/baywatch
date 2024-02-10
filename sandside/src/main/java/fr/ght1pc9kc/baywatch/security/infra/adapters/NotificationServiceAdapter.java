package fr.ght1pc9kc.baywatch.security.infra.adapters;

import fr.ght1pc9kc.baywatch.notify.api.NotifyService;
import fr.ght1pc9kc.baywatch.notify.api.model.BasicEvent;
import fr.ght1pc9kc.baywatch.notify.api.model.EventType;
import fr.ght1pc9kc.baywatch.notify.api.model.UserNotification;
import fr.ght1pc9kc.baywatch.security.domain.ports.NotificationPort;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Delegate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class NotificationServiceAdapter implements NotificationPort {
    private final NotifyService delegate;

    @Override
    public String send(String userId, EventType type, String data) {
        return delegate.send(userId, type, UserNotification.info(data)).id();
    }
}
