package fr.ght1pc9kc.baywatch.notify.api.model;

import lombok.Builder;
import lombok.NonNull;
import org.jetbrains.annotations.Nullable;

import java.time.Duration;

/**
 * Notification to the user. Can be anything targeted by a User.
 * Must by a {@link ServerEvent} of type {@link EventType#USER_NOTIFICATION}
 *
 * @param code     The notification code
 * @param severity The severity, on the front this define the color of notification
 * @param title    The Title of the notification
 * @param message  The message
 * @param delay    The delay the notification stay displayed before automatic acknowledge, in millis, default 5 000.
 * @param actions  Possible actions to be done on the target entity. For news :
 *                 <ul>
 *                 <li><strong>V</strong>: View</li>
 *                 <li><strong>S</strong>: Share</li>
 *                 <li><strong>C</strong>: Clip</li>
 *                 </ul>
 * @param target   The id of the targeted entity of actions
 */
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
