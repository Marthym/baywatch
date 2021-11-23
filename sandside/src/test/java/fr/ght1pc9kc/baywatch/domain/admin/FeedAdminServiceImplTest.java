package fr.ght1pc9kc.baywatch.domain.admin;

import fr.ght1pc9kc.baywatch.api.admin.FeedAdminService;
import fr.ght1pc9kc.baywatch.api.model.Feed;
import fr.ght1pc9kc.baywatch.api.security.model.Role;
import fr.ght1pc9kc.baywatch.api.security.model.User;
import fr.ght1pc9kc.baywatch.domain.exceptions.UnauthenticatedUser;
import fr.ght1pc9kc.baywatch.domain.exceptions.UnauthorizedOperation;
import fr.ght1pc9kc.baywatch.domain.ports.AuthenticationFacade;
import fr.ght1pc9kc.baywatch.domain.techwatch.model.QueryContext;
import fr.ght1pc9kc.baywatch.domain.techwatch.ports.FeedPersistencePort;
import fr.ght1pc9kc.baywatch.infra.model.FeedDeletedResult;
import fr.ght1pc9kc.baywatch.infra.samples.FeedRecordSamples;
import fr.ght1pc9kc.juery.api.Criteria;
import fr.ght1pc9kc.juery.api.PageRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.List;

import static fr.ght1pc9kc.baywatch.domain.FeedServiceImplTest.BAYWATCH_MAPPER;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class FeedAdminServiceImplTest {
    private static final User USER = User.builder().id("42").login("obiwan").role(Role.USER).build();
    private static final User ADMIN = User.builder().id("42").login("yoda").role(Role.ADMIN).build();

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
        when(mockAuthFacade.getConnectedUser()).thenReturn(Mono.just(USER));
        StepVerifier.create(tested.get("42")).verifyError(UnauthorizedOperation.class);
    }

    @Test
    void should_get_feed_as_admin() {
        when(mockAuthFacade.getConnectedUser()).thenReturn(Mono.just(ADMIN));
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
        when(mockAuthFacade.getConnectedUser()).thenReturn(Mono.just(USER));
        StepVerifier.create(tested.list()).verifyError(UnauthorizedOperation.class);
    }

    @Test
    void should_list_feed_for_admin() {
        when(mockAuthFacade.getConnectedUser()).thenReturn(Mono.just(ADMIN));
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
        when(mockAuthFacade.getConnectedUser()).thenReturn(Mono.just(USER));
        StepVerifier.create(tested.delete(List.of(FeedRecordSamples.JEDI.getFeedId()))).verifyError(UnauthorizedOperation.class);
    }

    @Test
    void should_delete_feed_for_admin() {
        when(mockAuthFacade.getConnectedUser()).thenReturn(Mono.just(ADMIN));

        StepVerifier.create(tested.delete(List.of(FeedRecordSamples.JEDI.getFeedId())))
                .expectNext(2)
                .verifyComplete();

        verify(mockFeedRepository, times(1)).delete(any());
    }
}