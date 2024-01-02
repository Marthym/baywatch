package fr.ght1pc9kc.baywatch.security.domain.ports;

import fr.ght1pc9kc.baywatch.notify.api.model.BasicEvent;
import fr.ght1pc9kc.baywatch.notify.api.model.EventType;

public interface NotificationPort {
    <T> BasicEvent<T> send(String userId, EventType type, T data);
}
