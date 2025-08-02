package fr.ght1pc9kc.baywatch.security.infra.adapters;

import fr.ght1pc9kc.baywatch.admin.api.FeedManagementService;
import fr.ght1pc9kc.baywatch.security.domain.model.PersonalFeed;
import fr.ght1pc9kc.baywatch.techwatch.api.FeedService;
import fr.ght1pc9kc.baywatch.tests.samples.UserSamples;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class TechwatchModuleAdapterTest {

    private TechwatchModuleAdapter tested;
    private final FeedService mockFeedService = mock(FeedService.class);
    private final FeedManagementService mockFeedManagementService = mock(FeedManagementService.class);

    @BeforeEach
    void setUp() {
        when(mockFeedService.addAndSubscribe(anyCollection())).thenReturn(Flux.empty());
        when(mockFeedService.unsubscribe(anyCollection())).thenReturn(Mono.empty());
        when(mockFeedManagementService.delete(anyCollection())).thenReturn(Mono.empty());

        tested = new TechwatchModuleAdapter(mockFeedService, mockFeedManagementService);
    }

    @Test
    void should_delegate_subscribe() {
        StepVerifier.create(tested.addAndSubscribePersonalFeed(PersonalFeed.of(UserSamples.OBIWAN)))
                .verifyComplete();

        verify(mockFeedService).addAndSubscribe(anyCollection());
    }

    @Test
    void should_delegate_unsubscribe() {
        StepVerifier.create(tested.unsubscribePersonalFeed(UserSamples.OBIWAN))
                .verifyComplete();

        verify(mockFeedService).unsubscribe(anyCollection());
    }

    @Test
    void should_delegate_delete() {
        StepVerifier.create(tested.deletePersonalFeed(UserSamples.OBIWAN))
                .verifyComplete();

        verify(mockFeedManagementService).delete(anyCollection());
    }
}