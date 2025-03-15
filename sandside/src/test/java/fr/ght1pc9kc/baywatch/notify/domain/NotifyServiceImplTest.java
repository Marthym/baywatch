package fr.ght1pc9kc.baywatch.notify.domain;

import com.github.f4b6a3.ulid.Ulid;
import fr.ght1pc9kc.baywatch.notify.api.model.BasicEvent;
import fr.ght1pc9kc.baywatch.notify.api.model.EventType;
import fr.ght1pc9kc.baywatch.notify.domain.ports.NotificationPersistencePort;
import fr.ght1pc9kc.baywatch.security.api.AuthenticationFacade;
import fr.ght1pc9kc.baywatch.tests.samples.UserSamples;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.codec.ServerSentEvent;
import reactor.core.Disposable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import static fr.ght1pc9kc.baywatch.tests.samples.UserSamples.LUKE;
import static fr.ght1pc9kc.baywatch.tests.samples.UserSamples.OBIWAN;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class NotifyServiceImplTest {

    private static final String TEST_EVENT_ID = "ULID-01";
    private static final ServerSentEvent<?> PING_EVENT = ServerSentEvent.builder()
            .id(TEST_EVENT_ID)
            .event(EventType.PING.getName())
            .build();

    private final AuthenticationFacade authFacadeMock = mock(AuthenticationFacade.class);
    private final NotificationPersistencePort notificationPersistenceMock = mock(NotificationPersistencePort.class);
    private NotifyServiceImpl tested;

    @BeforeEach
    @SuppressWarnings("unchecked")
    void setUp() {
        when(authFacadeMock.getConnectedUser()).thenReturn(
                Mono.just(OBIWAN),
                Mono.just(LUKE),
                Mono.just(OBIWAN),
                Mono.just(LUKE),
                Mono.just(OBIWAN),
                Mono.just(LUKE));

        when(notificationPersistenceMock.consume(anyString())).thenReturn(Flux.empty());
        when(notificationPersistenceMock.persist(any())).thenReturn(Mono.empty());
        tested = new NotifyServiceImpl(authFacadeMock, notificationPersistenceMock,
                () -> TEST_EVENT_ID,
                Clock.fixed(Instant.parse("2023-12-12T10:10:10Z"), ZoneOffset.UTC));
    }

    @Test
    void should_broadcast_notification() {
        List<ServerSentEvent<?>> actualObiwan = new CopyOnWriteArrayList<>();
        List<ServerSentEvent<?>> actualLuke = new CopyOnWriteArrayList<>();
        List<Throwable> errorsObiwan = new CopyOnWriteArrayList<>();
        List<Throwable> errorsLuke = new CopyOnWriteArrayList<>();

        Disposable disposableObiwan = Flux.<ServerSentEvent<?>>create(sink -> tested.subscribe(sink))
                .subscribe(actualObiwan::add, errorsObiwan::add);

        Disposable disposableLuke = Flux.<ServerSentEvent<?>>create(sink -> tested.subscribe(sink))
                .subscribe(actualLuke::add, errorsLuke::add);

        tested.broadcast(EventType.NEWS_UPDATE, 42);
        tested.send(LUKE.id(), EventType.USER_NOTIFICATION, "I'm your father");

        assertThat(disposableObiwan.isDisposed()).isFalse();
        assertThat(disposableLuke.isDisposed()).isFalse();

        disposableLuke.dispose();
        disposableObiwan.dispose();

        tested.broadcast(EventType.NEWS_UPDATE, 66);
        tested.close();

        assertThat(actualObiwan).containsExactly(PING_EVENT,
                ServerSentEvent.builder()
                        .id(TEST_EVENT_ID)
                        .event(EventType.NEWS_UPDATE.getName())
                        .data(42).build());
        assertThat(actualLuke).containsExactly(PING_EVENT,
                ServerSentEvent.builder()
                        .id(TEST_EVENT_ID)
                        .event(EventType.NEWS_UPDATE.getName())
                        .data(42).build(),
                ServerSentEvent.builder()
                        .id(TEST_EVENT_ID)
                        .event(EventType.USER_NOTIFICATION.getName())
                        .data("I'm your father").build());

        assertThat(errorsObiwan).isEmpty();
        assertThat(errorsLuke).isEmpty();

        disposableObiwan.dispose();
        disposableLuke.dispose();
        assertThat(disposableObiwan.isDisposed()).isTrue();
        assertThat(disposableLuke.isDisposed()).isTrue();
    }

    @Test
    void should_broadcast_notification_without_subscriber() {
        tested.broadcast(EventType.NEWS_UPDATE, Mono.just(42));
        tested.close();

        StepVerifier.create(Flux.<ServerSentEvent<?>>create(sink -> tested.subscribe(sink)))
                .verifyError();
    }

    @Test
    void should_send_notification_to_user() {
        when(authFacadeMock.getConnectedUser()).thenReturn(Mono.just(OBIWAN));

        List<ServerSentEvent<?>> actualObiwan = new CopyOnWriteArrayList<>();
        List<Throwable> errorsObiwan = new CopyOnWriteArrayList<>();
        Disposable disposable = Flux.<ServerSentEvent<?>>create(sink -> tested.subscribe(sink))
                .subscribe(actualObiwan::add, errorsObiwan::add);

        tested.send(UserSamples.OBIWAN.id(), EventType.USER_NOTIFICATION, "I'm your father");

        disposable.dispose();
        tested.close();

        assertThat(actualObiwan).containsExactly(PING_EVENT, ServerSentEvent.builder()
                .id(TEST_EVENT_ID)
                .event(EventType.USER_NOTIFICATION.getName())
                .data("I'm your father").build());
        assertThat(errorsObiwan).isEmpty();

        Awaitility.await("Await until disposed").atMost(Duration.ofSeconds(2)).until(disposable::isDisposed);
        assertThat(disposable.isDisposed()).isTrue();
    }

    @Test
    void should_get_pending_notifications_on_subscribe() {
        when(authFacadeMock.getConnectedUser()).thenReturn(Mono.just(OBIWAN));
        BasicEvent<String> event = new BasicEvent<>(Ulid.fast().toString(), EventType.USER_NOTIFICATION, "I'm your father");
        when(notificationPersistenceMock.consume(anyString()))
                .thenReturn(Flux.just(event));

        List<ServerSentEvent<?>> actualObiwan = new CopyOnWriteArrayList<>();
        List<Throwable> errorsObiwan = new CopyOnWriteArrayList<>();
        Disposable disposable = Flux.<ServerSentEvent<?>>create(sink -> tested.subscribe(sink))
                .subscribe(actualObiwan::add, errorsObiwan::add);

        disposable.dispose();
        tested.close();

        assertThat(actualObiwan).containsExactly(ServerSentEvent.builder()
                .id(event.id())
                .event(EventType.USER_NOTIFICATION.getName())
                .data("I'm your father").build(), PING_EVENT);
        assertThat(errorsObiwan).isEmpty();

        Awaitility.await("Await until disposed").atMost(Duration.ofSeconds(5)).until(disposable::isDisposed);
        assertThat(disposable.isDisposed()).isTrue();
    }

    @Test
    void should_store_notification_when_user_absent() {
        BasicEvent<String> event = new BasicEvent<>(Ulid.fast().toString(), EventType.USER_NOTIFICATION, "I'm your father");
        when(authFacadeMock.getConnectedUser()).thenReturn(Mono.just(OBIWAN));

        tested.send(UserSamples.OBIWAN.id(), EventType.USER_NOTIFICATION, "I'm your father");

        verify(notificationPersistenceMock).persist(any());

        reset(notificationPersistenceMock);
        when(notificationPersistenceMock.persist(any())).thenReturn(Mono.just(event));

        tested.send(UserSamples.OBIWAN.id(), EventType.USER_NOTIFICATION, Mono.just("I'm your father"));

        verify(notificationPersistenceMock).persist(any());
    }
}