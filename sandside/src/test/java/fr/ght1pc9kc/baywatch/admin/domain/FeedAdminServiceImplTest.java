package fr.ght1pc9kc.baywatch.admin.domain;

import fr.ght1pc9kc.baywatch.admin.api.FeedAdminService;
import fr.ght1pc9kc.baywatch.techwatch.api.model.Feed;
import fr.ght1pc9kc.baywatch.security.domain.exceptions.UnauthenticatedUser;
import fr.ght1pc9kc.baywatch.security.domain.exceptions.UnauthorizedOperation;
import fr.ght1pc9kc.baywatch.security.api.AuthenticationFacade;
import fr.ght1pc9kc.baywatch.tests.samples.UserSamples;
import fr.ght1pc9kc.baywatch.techwatch.domain.model.QueryContext;
import fr.ght1pc9kc.baywatch.techwatch.domain.ports.FeedPersistencePort;
import fr.ght1pc9kc.baywatch.techwatch.infra.model.FeedDeletedResult;
import fr.ght1pc9kc.baywatch.tests.samples.infra.FeedRecordSamples;
import fr.ght1pc9kc.juery.api.Criteria;
import fr.ght1pc9kc.juery.api.PageRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.List;

import static fr.ght1pc9kc.baywatch.techwatch.domain.FeedServiceImplTest.BAYWATCH_MAPPER;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class FeedAdminServiceImplTest {
    private FeedAdminService tested;

    private final FeedPersistencePort mockFeedRepository = mock(FeedPersistencePort.class);
    private final AuthenticationFacade mockAuthFacade = mock(AuthenticationFacade.class);

    @BeforeEach
    void setUp() {
        tested = new FeedAdminServiceImpl(mockFeedRepository, mockAuthFacade);
        Feed jediFeed = BAYWATCH_MAPPER.recordToFeed(FeedRecordSamples.JEDI);
        when(mockFeedRepository.get(any())).thenReturn(Mono.just(jediFeed));
        when(mockFeedRepository.list(any())).thenReturn(Flux.just(jediFeed));
        when(mockFeedRepository.delete(any())).thenReturn(Mono.just(new FeedDeletedResult(1, 2)));
    }

    @Test
    void should_get_feed_as_user() {
        when(mockAuthFacade.getConnectedUser()).thenReturn(Mono.just(UserSamples.LUKE));
        StepVerifier.create(tested.get("42")).verifyError(UnauthorizedOperation.class);
    }

    @Test
    void should_get_feed_as_admin() {
        when(mockAuthFacade.getConnectedUser()).thenReturn(Mono.just(UserSamples.YODA));
        StepVerifier.create(tested.get("42"))
                .expectNextMatches(r -> FeedRecordSamples.JEDI.getFeedId().equals(r.getId()))
                .verifyComplete();
        verify(mockFeedRepository, times(1)).get(any());
    }

    @Test
    void should_list_feed_for_anonymous() {
        when(mockAuthFacade.getConnectedUser()).thenReturn(Mono.empty());
        StepVerifier.create(tested.list()).verifyError(UnauthenticatedUser.class);
    }

    @Test
    void should_list_feed_for_user() {
        when(mockAuthFacade.getConnectedUser()).thenReturn(Mono.just(UserSamples.LUKE));
        StepVerifier.create(tested.list()).verifyError(UnauthorizedOperation.class);
    }

    @Test
    void should_list_feed_for_admin() {
        when(mockAuthFacade.getConnectedUser()).thenReturn(Mono.just(UserSamples.YODA));
        ArgumentCaptor<QueryContext> captor = ArgumentCaptor.forClass(QueryContext.class);

        {
            StepVerifier.create(tested.list())
                    .expectNextMatches(r -> r.getId().equals(FeedRecordSamples.JEDI.getFeedId()))
                    .verifyComplete();

            verify(mockFeedRepository, times(1)).list(captor.capture());
            assertThat(captor.getValue()).isEqualTo(QueryContext.empty());
        }

        {
            clearInvocations(mockFeedRepository);
            StepVerifier.create(tested.list(PageRequest.one(Criteria.property("name").eq("jedi"))))
                    .expectNextMatches(r -> r.getId().equals(FeedRecordSamples.JEDI.getFeedId()))
                    .verifyComplete();

            verify(mockFeedRepository, times(1)).list(captor.capture());
            assertThat(captor.getValue()).isEqualTo(QueryContext.first(
                    Criteria.property("name").eq("jedi")));
        }
    }

    @Test
    void should_delete_feed_for_user() {
        when(mockAuthFacade.getConnectedUser()).thenReturn(Mono.just(UserSamples.LUKE));
        StepVerifier.create(tested.delete(List.of(FeedRecordSamples.JEDI.getFeedId()))).verifyError(UnauthorizedOperation.class);
    }

    @Test
    void should_delete_feed_for_admin() {
        when(mockAuthFacade.getConnectedUser()).thenReturn(Mono.just(UserSamples.YODA));

        StepVerifier.create(tested.delete(List.of(FeedRecordSamples.JEDI.getFeedId())))
                .expectNext(2)
                .verifyComplete();

        verify(mockFeedRepository, times(1)).delete(any());
    }
}