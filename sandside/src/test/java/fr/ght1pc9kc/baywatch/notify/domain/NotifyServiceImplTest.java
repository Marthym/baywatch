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
    void setUp() {
        AuthenticationFacade authFacadeMock = mock(AuthenticationFacade.class);
        when(authFacadeMock.getConnectedUser()).thenReturn(Mono.just(UserSamples.OBIWAN));
        tested = new NotifyServiceImpl(authFacadeMock);
    }

    @Test
    void should_send_notification() {
        List<ServerEvent<Object>> actual = new CopyOnWriteArrayList<>();
        List<Throwable> errors = new CopyOnWriteArrayList<>();
        AtomicBoolean isComplete = new AtomicBoolean(false);

        tested.subscribe().subscribe(
                actual::add, errors::add,
                () -> isComplete.set(true)
        );
        ServerEvent<Object> event1 = tested.broadcast(EventType.NEWS, 42);
        tested.unsubscribe().block();
        tested.broadcast(EventType.NEWS, Mono.just(66));
        tested.close();

        Assertions.assertThat(actual).containsExactly(event1);
        Assertions.assertThat(errors).isEmpty();
        Assertions.assertThat(isComplete).isTrue();
    }

    @Test
    void should_send_notification_without_subscriber() {
        tested.broadcast(EventType.NEWS, Mono.just(42));
        tested.close();

        StepVerifier.create(tested.subscribe()).verifyComplete();
    }
}