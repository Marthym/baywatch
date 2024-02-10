package fr.ght1pc9kc.baywatch.security.domain.ports;

import fr.ght1pc9kc.baywatch.notify.api.model.EventType;

public interface NotificationPort {
    String send(String userId, EventType type, String data);
}
