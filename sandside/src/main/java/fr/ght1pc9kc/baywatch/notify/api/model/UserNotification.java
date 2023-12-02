package fr.ght1pc9kc.baywatch.notify.api.model;

import lombok.Builder;
import lombok.NonNull;
import org.jetbrains.annotations.Nullable;

import java.time.Duration;

@Builder(toBuilder = true)
public record UserNotification(
        @NonNull String code,
        @NonNull Severity severity,
        @Nullable String title,
        String message,
        long delay,
        String actions,
        String target
) {
    public static final String CODE_OK = "OK";
    public static final String CODE_ERROR = "ERROR";
    public static final String CODE_NEWS_ADD = "NEWS_ADD";

    public static final long DELAY_DEFAULT = Duration.ofSeconds(10).toMillis();
    public static final long DELAY_UNLIMITED = -1;

    public static UserNotification info(String message) {
        return UserNotification.builder()
                .code(CODE_OK)
                .severity(Severity.info)
                .message(message)
                .delay(DELAY_DEFAULT)
                .build();
    }

    public static UserNotification error(String message) {
        return UserNotification.builder()
                .code(CODE_ERROR)
                .severity(Severity.error)
                .message(message)
                .delay(DELAY_DEFAULT)
                .build();
    }
}
