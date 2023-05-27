package fr.ght1pc9kc.baywatch.scraper.domain.actions;

import fr.ght1pc9kc.baywatch.notify.api.NotifyService;
import fr.ght1pc9kc.baywatch.notify.api.model.EventType;
import fr.ght1pc9kc.baywatch.scraper.api.model.ScrapResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class NewsUpdateNotificationHandlerTest {
    private NewsUpdateNotificationHandler tested;

    private NotifyService notifyServiceMock;

    @BeforeEach
    void setUp() {
        notifyServiceMock = mock(NotifyService.class);
        tested = new NewsUpdateNotificationHandler(notifyServiceMock);
    }

    @Test
    void should_call_after() {
        tested.after(new ScrapResult(1, List.of())).block();
        verify(notifyServiceMock).broadcast(EventType.NEWS_UPDATE, "UPDATED");
    }
}