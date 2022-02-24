package fr.ght1pc9kc.baywatch.notify.domain;

import fr.ght1pc9kc.baywatch.notify.api.model.EventType;
import fr.ght1pc9kc.baywatch.security.api.AuthenticationFacade;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.concurrent.atomic.AtomicReference;

import static org.mockito.Mockito.mock;

class NotifyServiceImplTest {

    private NotifyServiceImpl tested;
    private AuthenticationFacade authFacadeMock;

    @BeforeEach
    void setUp() {
        authFacadeMock = mock(AuthenticationFacade.class);
        tested = new NotifyServiceImpl(authFacadeMock);
    }

    @Test
    void should_send_notification() {
        AtomicReference<EventType> actual = new AtomicReference<>();
        tested.subscribe().subscribe(t -> actual.set(t.type()));
        tested.send(EventType.NEWS, Mono.just(42));
        tested.close();

        Assertions.assertThat(actual.get()).isEqualTo(EventType.NEWS);
    }

    @Test
    void should_send_notification_without_subscriber() {
        tested.send(EventType.NEWS, Mono.just(42));
        tested.close();

        StepVerifier.create(tested.subscribe()).verifyComplete();
    }
}