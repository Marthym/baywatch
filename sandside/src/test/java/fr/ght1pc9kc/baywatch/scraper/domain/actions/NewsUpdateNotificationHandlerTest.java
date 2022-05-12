package fr.ght1pc9kc.baywatch.scraper.domain.actions;

import fr.ght1pc9kc.baywatch.notify.api.NotifyService;
import fr.ght1pc9kc.baywatch.notify.api.model.EventType;
import fr.ght1pc9kc.baywatch.techwatch.api.NewsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import reactor.core.publisher.Mono;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class NewsUpdateNotificationHandlerTest {
    private NewsUpdateNotificationHandler tested;

    private NotifyService notifyServiceMock;
    private NewsService newsServiceMock;

    @BeforeEach
    void setUp() {
        notifyServiceMock = mock(NotifyService.class);
        newsServiceMock = mock(NewsService.class);
        when(newsServiceMock.count(any())).thenReturn(Mono.just(42));
        tested = new NewsUpdateNotificationHandler(notifyServiceMock, newsServiceMock);
    }

    @Test
    @SuppressWarnings("unchecked")
    void should_call_after() {
        tested.after(1).block();
        ArgumentCaptor<Mono<Object>> captor = ArgumentCaptor.forClass(Mono.class);
        verify(notifyServiceMock).send(eq(EventType.NEWS), captor.capture());

        captor.getValue().block();

        verify(newsServiceMock).count(any());
    }
}