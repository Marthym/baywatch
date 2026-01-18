package fr.ght1pc9kc.baywatch.security.infra.adapters.subscribers;

import fr.ght1pc9kc.baywatch.security.api.UserService;
import fr.ght1pc9kc.baywatch.security.domain.ports.NotificationPort;
import fr.ght1pc9kc.baywatch.security.domain.ports.UserEventPublisherPort;
import fr.ght1pc9kc.baywatch.security.domain.subscribers.AdminNotifyUserSubscriber;
import lombok.experimental.Delegate;
import org.springframework.stereotype.Component;

@Component
public class AdminNotifyUserSubscriberAdapter {
    @Delegate
    private final AdminNotifyUserSubscriber delegate;

    public AdminNotifyUserSubscriberAdapter(
            UserEventPublisherPort userPublisher, UserService userService, NotificationPort notificationPort) {
        delegate = new AdminNotifyUserSubscriber(userPublisher, userService, notificationPort);
    }
}
