package fr.ght1pc9kc.baywatch.notify.domain;

import com.github.f4b6a3.ulid.Ulid;
import fr.ght1pc9kc.baywatch.notify.api.model.BasicEvent;
import fr.ght1pc9kc.baywatch.notify.api.model.EventType;
import fr.ght1pc9kc.baywatch.notify.api.model.ServerEvent;
import fr.ght1pc9kc.baywatch.notify.domain.ports.NotificationPersistencePort;
import fr.ght1pc9kc.baywatch.security.api.AuthenticationFacade;
import fr.ght1pc9kc.baywatch.tests.samples.UserSamples;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import reactor.core.Disposable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.Clock;
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

    private NotifyServiceImpl tested;
    private final AuthenticationFacade authFacadeMock = mock(AuthenticationFacade.class);
    private final NotificationPersistencePort notificationPersistenceMock = mock(NotificationPersistencePort.class);

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
        tested = new NotifyServiceImpl(authFacadeMock, notificationPersistenceMock, Clock.fixed(Instant.parse("2023-12-12T10:10:10Z"), ZoneOffset.UTC));
    }

    @Test
    void should_broadcast_notification() {
        List<ServerEvent> actualObiwan = new CopyOnWriteArrayList<>();
        List<ServerEvent> actualLuke = new CopyOnWriteArrayList<>();
        List<Throwable> errorsObiwan = new CopyOnWriteArrayList<>();
        List<Throwable> errorsLuke = new CopyOnWriteArrayList<>();

        Disposable disposableObiwan = tested.subscribe().subscribe(
                actualObiwan::add, errorsObiwan::add);
        Disposable disposableLuke = tested.subscribe().subscribe(
                actualLuke::add, errorsLuke::add);

        ServerEvent eventBroadcast = tested.broadcast(EventType.NEWS_UPDATE, 42);
        ServerEvent eventUser = tested.send(LUKE.id(), EventType.USER_NOTIFICATION, "I'm your father");

        assertThat(disposableObiwan.isDisposed()).isFalse();
        assertThat(disposableLuke.isDisposed()).isFalse();

        StepVerifier.create(tested.unsubscribe()).expectNext(true).verifyComplete();
        StepVerifier.create(tested.unsubscribe()).expectNext(true).verifyComplete();

        tested.broadcast(EventType.NEWS_UPDATE, 66);
        tested.close();

        assertThat(actualObiwan).containsExactly(eventBroadcast);
        assertThat(actualLuke).containsExactly(eventBroadcast, eventUser);

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
        StepVerifier.create(tested.subscribe()).verifyError();
    }

    @Test
    void should_send_notification_to_user() {
        when(authFacadeMock.getConnectedUser()).thenReturn(Mono.just(OBIWAN));

        List<ServerEvent> actualObiwan = new CopyOnWriteArrayList<>();
        List<Throwable> errorsObiwan = new CopyOnWriteArrayList<>();
        Disposable disposable = tested.subscribe().subscribe(
                actualObiwan::add, errorsObiwan::add);

        ServerEvent eventUser = tested.send(UserSamples.OBIWAN.id(), EventType.USER_NOTIFICATION, "I'm your father");

        tested.close();

        assertThat(actualObiwan).containsExactly(eventUser);
        assertThat(errorsObiwan).isEmpty();
        assertThat(disposable.isDisposed()).isTrue();
    }

    @Test
    void should_get_pending_notifications_on_subscribe() {
        when(authFacadeMock.getConnectedUser()).thenReturn(Mono.just(OBIWAN));
        BasicEvent<String> event = new BasicEvent<>(Ulid.fast().toString(), EventType.USER_NOTIFICATION, "I'm your father");
        when(notificationPersistenceMock.consume(anyString()))
                .thenReturn(Flux.just(event));

        List<ServerEvent> actualObiwan = new CopyOnWriteArrayList<>();
        List<Throwable> errorsObiwan = new CopyOnWriteArrayList<>();
        Disposable disposable = tested.subscribe().subscribe(
                actualObiwan::add, errorsObiwan::add);

        tested.close();

        assertThat(actualObiwan).containsExactly(event);
        assertThat(errorsObiwan).isEmpty();
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