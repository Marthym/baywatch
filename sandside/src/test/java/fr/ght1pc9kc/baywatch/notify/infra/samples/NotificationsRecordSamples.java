package fr.ght1pc9kc.baywatch.notify.infra.samples;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import fr.ght1pc9kc.baywatch.dsl.tables.records.NotificationsRecord;
import fr.ght1pc9kc.baywatch.notify.api.model.EventType;
import fr.ght1pc9kc.baywatch.notify.api.model.Severity;
import fr.ght1pc9kc.baywatch.notify.api.model.UserNotification;
import fr.ght1pc9kc.baywatch.tests.samples.FeedSamples;
import fr.ght1pc9kc.baywatch.tests.samples.UserSamples;
import fr.ght1pc9kc.testy.jooq.model.RelationalDataSet;

import java.time.LocalDateTime;
import java.util.List;

import static fr.ght1pc9kc.baywatch.dsl.tables.Notifications.NOTIFICATIONS;

public class NotificationsRecordSamples implements RelationalDataSet<NotificationsRecord> {
    public static final NotificationsRecordSamples SAMPLE = new NotificationsRecordSamples();

    public static final UserNotification USER_NOTIFICATION = UserNotification.builder()
            .code(UserNotification.CODE_NEWS_ADD)
            .severity(Severity.info)
            .title("The new light saber was arrived")
            .message("Hello your new light saber was arrived, you can go to get it !")
            .actions("VSC")
            .target(FeedSamples.JEDI.id())
            .build();

    public static final NotificationsRecord DUMMY_NOTIFICATION_RECORD = NOTIFICATIONS.newRecord()
            .setNotiId("EV01HHA6PFESHHFK4YHT1T2HKHSQ")
            .setNotiUserId(UserSamples.MWINDU.id())
            .setNotiEventType(EventType.USER_NOTIFICATION.name())
            .setNotiData("String")
            .setNotiCreatedAt(LocalDateTime.now());
    public static final NotificationsRecord USER_NOTIFICATIONS_RECORD;

    static {
        ObjectMapper json = new ObjectMapper();
        try {
            USER_NOTIFICATIONS_RECORD = NOTIFICATIONS.newRecord()
                    .setNotiId("EV01HHA6PFESHHFK4YHT1T2HKHSR")
                    .setNotiUserId(UserSamples.OBIWAN.id())
                    .setNotiEventType(EventType.USER_NOTIFICATION.name())
                    .setNotiData(json.writeValueAsString(USER_NOTIFICATION))
                    .setNotiCreatedAt(LocalDateTime.now());
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<NotificationsRecord> records() {
        return List.of(USER_NOTIFICATIONS_RECORD, DUMMY_NOTIFICATION_RECORD);
    }
}
