package fr.ght1pc9kc.baywatch.notify.api.model;

import java.time.Duration;

public record UserNotification(
        String code,
        String severity,
        String message,
        long delay
) {
    public static UserNotification info(String message) {
        return new UserNotification("OK", "info", message, Duration.ofSeconds(10).toMillis());
    }

    public static UserNotification error(String message) {
        return new UserNotification("ERROR", "error", message, Duration.ofSeconds(10).toMillis());
    }
}
