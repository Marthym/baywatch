package fr.ght1pc9kc.baywatch.scrapper.domain.actions;

import fr.ght1pc9kc.baywatch.techwatch.api.StatService;
import fr.ght1pc9kc.baywatch.notify.api.model.EventType;
import fr.ght1pc9kc.baywatch.notify.api.NotifyService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import reactor.core.publisher.Mono;

import static org.mockito.Mockito.*;

class NewsUpdateNotificationHandlerTest {
    private NewsUpdateNotificationHandler tested;

    private NotifyService notifyServiceMock;
    private StatService statServiceMock;

    @BeforeEach
    void setUp() {
        notifyServiceMock = mock(NotifyService.class);
        statServiceMock = spy(new StatService() {
            @Override
            public Mono<Integer> getNewsCount() {
                return Mono.just(42);
            }

            @Override
            public Mono<Integer> getFeedsCount() {
                return Mono.just(24);
            }

            @Override
            public Mono<Integer> getUnreadCount() {
                return Mono.just(12);
            }
        });
        tested = new NewsUpdateNotificationHandler(notifyServiceMock, statServiceMock);
    }

    @Test
    @SuppressWarnings("unchecked")
    void should_call_after() {
        tested.after(1).block();
        ArgumentCaptor<Mono<Object>> captor = ArgumentCaptor.forClass(Mono.class);
        verify(notifyServiceMock).send(eq(EventType.NEWS), captor.capture());

        captor.getValue().block();

        verify(statServiceMock).getFeedsCount();
        verify(statServiceMock).getNewsCount();
        verify(statServiceMock).getUnreadCount();
    }
}