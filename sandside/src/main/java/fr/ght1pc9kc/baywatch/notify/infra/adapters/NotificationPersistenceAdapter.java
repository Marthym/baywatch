package fr.ght1pc9kc.baywatch.notify.infra.adapters;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.MapLikeType;
import com.machinezoo.noexception.Exceptions;
import fr.ght1pc9kc.baywatch.common.api.model.Entity;
import fr.ght1pc9kc.baywatch.common.domain.DateUtils;
import fr.ght1pc9kc.baywatch.common.infra.DatabaseQualifier;
import fr.ght1pc9kc.baywatch.dsl.tables.records.NotificationsRecord;
import fr.ght1pc9kc.baywatch.notify.api.model.BasicEvent;
import fr.ght1pc9kc.baywatch.notify.api.model.EventType;
import fr.ght1pc9kc.baywatch.notify.api.model.ReactiveEvent;
import fr.ght1pc9kc.baywatch.notify.api.model.ServerEvent;
import fr.ght1pc9kc.baywatch.notify.api.model.ServerEventVisitor;
import fr.ght1pc9kc.baywatch.notify.domain.ports.NotificationPersistencePort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jooq.DSLContext;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;

import java.util.Map;
import java.util.Optional;

import static fr.ght1pc9kc.baywatch.dsl.tables.Notifications.NOTIFICATIONS;

@Slf4j
@Repository
@RequiredArgsConstructor
public class NotificationPersistenceAdapter implements NotificationPersistencePort {
    private final @DatabaseQualifier Scheduler databaseScheduler;
    private final DSLContext dsl;
    private final ObjectMapper jsonMapper;

    @Override
    public Mono<ServerEvent> persist(Entity<ServerEvent> event) {
        Mono<NotificationsRecord> notificationsRecord = event.self().accept(new ServerEventVisitor<>() {
            @Override
            public <T> Mono<NotificationsRecord> visit(BasicEvent<T> event) {
                return Mono.just(NOTIFICATIONS.newRecord()
                        .setNotiData(Exceptions.wrap().get(() -> jsonMapper.writeValueAsString(event.message()))));
            }

            @Override
            public <T> Mono<NotificationsRecord> visit(ReactiveEvent<T> event) {
                return event.message().map(data -> Exceptions.wrap().get(() -> jsonMapper.writeValueAsString(data)))
                        .map(message -> NOTIFICATIONS.newRecord().setNotiData(message));
            }
        });
        return notificationsRecord.map(r ->
                        r.setNotiCreatedAt(DateUtils.toLocalDateTime(event.createdAt()))
                                .setNotiEventType(event.self().type().getName())
                                .setNotiId(event.id())
                                .setNotiUserId(event.createdBy()))
                .flatMap(r ->
                        Mono.fromCompletionStage(dsl.insertInto(NOTIFICATIONS).set(r).executeAsync())
                                .subscribeOn(databaseScheduler))
                .thenReturn(event.self());
    }

    @Override
    @SuppressWarnings({"BlockingMethodInNonBlockingContext", "resource"})
    public Flux<ServerEvent> consume(String userId) {
        var select = dsl.selectFrom(NOTIFICATIONS).where(NOTIFICATIONS.NOTI_USER_ID.eq(userId));

        return Flux.<NotificationsRecord>create(sink -> {
                    var cursor = select.fetchLazy();
                    sink.onRequest(n -> {
                        int count = (int) n;
                        var rs = cursor.fetchNext(count);
                        rs.forEach(sink::next);
                        if (rs.size() < count) {
                            sink.complete();
                        }
                    }).onDispose(cursor::close);
                }).limitRate(Integer.MAX_VALUE - 1).subscribeOn(databaseScheduler)
                .flatMap(r -> Mono.fromCompletionStage(dsl.deleteFrom(NOTIFICATIONS)
                                .where(NOTIFICATIONS.NOTI_ID.eq(r.getNotiId()))
                                .executeAsync())
                        .then(Mono.just(r)))
                .map(this::buildServerEvent);
    }

    private ServerEvent buildServerEvent(NotificationsRecord noti) {
        MapLikeType dataType = jsonMapper.getTypeFactory().constructMapLikeType(
                Map.class, String.class, Object.class);
        Map<String, String> message = Optional.ofNullable(noti.getNotiData()).map(data -> {
            try {
                return jsonMapper.readValue(noti.getNotiData(), dataType);
            } catch (Exception e) {
                return Map.of("message", data);
            }
        }).orElse(Map.of());
        return new BasicEvent<>(
                noti.getNotiId(),
                EventType.valueOf(noti.getNotiEventType()),
                message);
    }
}
