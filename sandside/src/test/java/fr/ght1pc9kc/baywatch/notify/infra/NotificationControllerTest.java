package fr.ght1pc9kc.baywatch.notify.infra;

import fr.ght1pc9kc.baywatch.notify.api.NotifyManager;
import fr.ght1pc9kc.baywatch.notify.api.model.EventType;
import fr.ght1pc9kc.baywatch.notify.api.model.UserNotification;
import fr.ght1pc9kc.baywatch.notify.infra.controllers.NotificationController;
import fr.ght1pc9kc.baywatch.security.api.AuthenticationFacade;
import fr.ght1pc9kc.baywatch.security.api.model.User;
import fr.ght1pc9kc.entity.api.Entity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class NotificationControllerTest {
    private NotificationController tested;
    private final NotifyManager mockNotifyManager = mock(NotifyManager.class);
    private final AuthenticationFacade mockFacade = mock(AuthenticationFacade.class);

    @BeforeEach
    void setUp() {
        doReturn(Mono.just(Entity.identify(new User("okenobi", "Obiwan", "okenobi@botdesign.net", "pass", List.of()))
                .withId("42")))
                .when(mockFacade).getConnectedUser();
        tested = new NotificationController(mockNotifyManager, mockFacade);
    }

    @Test
    void should_test_sse() {
        StepVerifier.create(tested.test("salut"))
                .verifyComplete();

        verify(mockNotifyManager).broadcast(EventType.NEWS_UPDATE, "UPDATE salut");
        verify(mockNotifyManager).send(anyString(), eq(EventType.USER_NOTIFICATION), any(UserNotification.class));
    }
}