package fr.ght1pc9kc.baywatch.notify.domain;

import fr.ght1pc9kc.baywatch.notify.api.model.EventType;
import fr.ght1pc9kc.baywatch.notify.api.model.ServerEvent;
import fr.ght1pc9kc.baywatch.security.api.AuthenticationFacade;
import fr.ght1pc9kc.baywatch.tests.samples.UserSamples;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class NotifyServiceImplTest {

    private NotifyServiceImpl tested;

    @BeforeEach
    @SuppressWarnings("unchecked")
    void setUp() {
        AuthenticationFacade authFacadeMock = mock(AuthenticationFacade.class);
        when(authFacadeMock.getConnectedUser()).thenReturn(
                Mono.just(UserSamples.OBIWAN),
                Mono.just(UserSamples.LUKE),
                Mono.just(UserSamples.OBIWAN),
                Mono.just(UserSamples.LUKE),
                Mono.just(UserSamples.OBIWAN),
                Mono.just(UserSamples.LUKE));
        tested = new NotifyServiceImpl(authFacadeMock);
    }

    @Test
    void should_broadcast_notification() {
        List<ServerEvent> actualObiwan = new CopyOnWriteArrayList<>();
        List<ServerEvent> actualLuke = new CopyOnWriteArrayList<>();
        List<Throwable> errorsObiwan = new CopyOnWriteArrayList<>();
        List<Throwable> errorsLuke = new CopyOnWriteArrayList<>();
        AtomicBoolean isObiwanComplete = new AtomicBoolean(false);
        AtomicBoolean isLukeComplete = new AtomicBoolean(false);

        tested.subscribe().doOnTerminate(() -> isObiwanComplete.set(true)).subscribe(
                actualObiwan::add, errorsObiwan::add,
                () -> isObiwanComplete.set(true)
        );
        tested.subscribe().doOnCancel(() -> isLukeComplete.set(true)).subscribe(
                actualLuke::add, errorsLuke::add,
                () -> isLukeComplete.set(true)
        );
        ServerEvent eventBroadcast = tested.broadcast(EventType.NEWS_UPDATE, 42);
        ServerEvent eventUser = tested.send(UserSamples.LUKE.id, EventType.USER_NOTIFICATION, "I'm your father");
        tested.unsubscribe().block();
        tested.unsubscribe().block();

        tested.broadcast(EventType.NEWS_UPDATE, Mono.just(66));
        tested.close();

        Assertions.assertThat(actualObiwan).containsExactly(eventBroadcast);
        Assertions.assertThat(actualLuke).containsExactly(eventBroadcast, eventUser);

        Assertions.assertThat(errorsObiwan).isEmpty();
        Assertions.assertThat(errorsLuke).isEmpty();

        Assertions.assertThat(isObiwanComplete).isTrue();
        Assertions.assertThat(isLukeComplete).isTrue();
    }

    @Test
    void should_send_notification_without_subscriber() {
        tested.broadcast(EventType.NEWS_UPDATE, Mono.just(42));
        tested.close();
        StepVerifier.create(tested.subscribe()).verifyError();
    }
}