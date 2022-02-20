package fr.ght1pc9kc.baywatch.notify.domain;

import fr.ght1pc9kc.baywatch.notify.api.EventType;
import fr.ght1pc9kc.baywatch.notify.api.NotifyService;
import fr.ght1pc9kc.baywatch.notify.domain.NotifyServiceImpl;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.concurrent.atomic.AtomicReference;

class NotifyServiceImplTest {

    private final NotifyService tested = new NotifyServiceImpl();

    @Test
    void should_send_notification() {
        AtomicReference<EventType> actual = new AtomicReference<>();
        tested.getFlux().subscribe(t -> actual.set(t.getT1()));
        tested.send(EventType.NEWS, Mono.just(42));
        tested.close();

        Assertions.assertThat(actual.get()).isEqualTo(EventType.NEWS);
    }

    @Test
    void should_send_notification_without_subscriber() {
        tested.send(EventType.NEWS, Mono.just(42));
        tested.close();

        StepVerifier.create(tested.getFlux()).verifyComplete();
    }
}