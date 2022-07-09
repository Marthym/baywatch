package fr.ght1pc9kc.baywatch.techwatch.domain;

import fr.ght1pc9kc.baywatch.common.api.model.Entity;
import fr.ght1pc9kc.baywatch.common.infra.mappers.BaywatchMapper;
import fr.ght1pc9kc.baywatch.security.api.AuthenticationFacade;
import fr.ght1pc9kc.baywatch.security.api.model.User;
import fr.ght1pc9kc.baywatch.security.domain.exceptions.UnauthenticatedUser;
import fr.ght1pc9kc.baywatch.techwatch.api.FeedService;
import fr.ght1pc9kc.baywatch.techwatch.api.model.Feed;
import fr.ght1pc9kc.baywatch.techwatch.domain.model.QueryContext;
import fr.ght1pc9kc.baywatch.techwatch.domain.ports.FeedPersistencePort;
import fr.ght1pc9kc.baywatch.techwatch.domain.ports.ScraperServicePort;
import fr.ght1pc9kc.baywatch.tests.samples.FeedSamples;
import fr.ght1pc9kc.baywatch.tests.samples.UserSamples;
import fr.ght1pc9kc.baywatch.tests.samples.infra.FeedRecordSamples;
import fr.ght1pc9kc.baywatch.tests.samples.infra.UsersRecordSamples;
import fr.ght1pc9kc.juery.api.Criteria;
import fr.ght1pc9kc.juery.api.PageRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import org.mockito.ArgumentCaptor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.clearInvocations;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class FeedServiceImplTest {
    public static final BaywatchMapper BAYWATCH_MAPPER = Mappers.getMapper(BaywatchMapper.class);

    private final FeedPersistencePort mockFeedRepository = mock(FeedPersistencePort.class);
    private final AuthenticationFacade mockAuthFacade = mock(AuthenticationFacade.class);

    private FeedService tested;

    @BeforeEach
    void setUp() {
        Feed jediFeed = BAYWATCH_MAPPER.recordToFeed(FeedRecordSamples.JEDI);
        when(mockFeedRepository.get(any())).thenReturn(Mono.just(jediFeed));
        when(mockFeedRepository.list(any())).thenReturn(Flux.just(jediFeed));
        when(mockFeedRepository.persist(any())).thenReturn(Flux.empty().then());
        when(mockFeedRepository.persist(any(), any())).thenReturn(Flux.empty().then());
        when(mockFeedRepository.count(any())).thenReturn(Mono.just(42));

        ScraperServicePort mockScraperService = mock(ScraperServicePort.class);
        when(mockScraperService.fetchFeedData(any())).thenReturn(Mono.just(FeedSamples.JEDI));
        tested = new FeedServiceImpl(mockFeedRepository, mockScraperService, mockAuthFacade);
    }

    @Test
    void should_get_feed() {
        tested.get("42").block();
        verify(mockFeedRepository, times(1)).get(any());
    }

    @Test
    void should_list_feed_for_anonymous() {
        when(mockAuthFacade.getConnectedUser()).thenReturn(Mono.empty());
        ArgumentCaptor<QueryContext> captor = ArgumentCaptor.forClass(QueryContext.class);

        tested.list().collectList().block();

        verify(mockFeedRepository, times(1)).list(captor.capture());

        assertThat(captor.getValue()).isEqualTo(QueryContext.empty());
    }

    @Test
    void should_list_feed_for_user() {
        Entity<User> okenobi = BAYWATCH_MAPPER.recordToUserEntity(UsersRecordSamples.OKENOBI);
        when(mockAuthFacade.getConnectedUser()).thenReturn(Mono.just(okenobi));
        ArgumentCaptor<QueryContext> captor = ArgumentCaptor.forClass(QueryContext.class);

        {
            tested.list().collectList().block();

            verify(mockFeedRepository, times(1)).list(captor.capture());
            assertThat(captor.getValue()).isEqualTo(QueryContext.empty().withUserId(okenobi.id));
        }

        {
            clearInvocations(mockFeedRepository);
            tested.list(PageRequest.one(Criteria.property("name").eq("jedi"))).collectList().block();

            verify(mockFeedRepository, times(1)).list(captor.capture());
            assertThat(captor.getValue()).isEqualTo(QueryContext.first(
                    Criteria.property("name").eq("jedi")).withUserId(okenobi.id));
        }
    }

    @Test
    void should_persist_feeds_for_anonymous() {
        when(mockAuthFacade.getConnectedUser()).thenReturn(Mono.empty());
        StepVerifier.create(tested.persist(List.of())).verifyError(UnauthenticatedUser.class);
    }

    @Test
    @SuppressWarnings("unchecked")
    void should_persist_feeds_for_user() {
        Entity<User> okenobi = BAYWATCH_MAPPER.recordToUserEntity(UsersRecordSamples.OKENOBI);
        when(mockAuthFacade.getConnectedUser()).thenReturn(Mono.just(okenobi));
        ArgumentCaptor<List<Feed>> captor = ArgumentCaptor.forClass(List.class);

        StepVerifier.create(tested.persist(List.of(FeedSamples.JEDI)))
                .verifyComplete();

        verify(mockFeedRepository, times(1)).persist(captor.capture(),
                eq(UsersRecordSamples.OKENOBI.getUserId()));
        assertThat(captor.getValue()).containsExactly(FeedSamples.JEDI);
    }

    @Test
    void should_persist_unsecured_url() {
        when(mockAuthFacade.getConnectedUser()).thenReturn(Mono.just(UserSamples.OBIWAN));
        StepVerifier.create(tested.persist(List.of(FeedSamples.UNSECURE_PROTOCOL)))
                .verifyError(IllegalArgumentException.class);

        StepVerifier.create(tested.update(FeedSamples.UNSECURE_PROTOCOL))
                .verifyError(IllegalArgumentException.class);

    }
}
