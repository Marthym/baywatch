package fr.ght1pc9kc.baywatch.techwatch.domain;

import fr.ght1pc9kc.baywatch.techwatch.api.FeedService;
import fr.ght1pc9kc.baywatch.common.api.model.Entity;
import fr.ght1pc9kc.baywatch.techwatch.api.model.Feed;
import fr.ght1pc9kc.baywatch.security.api.model.User;
import fr.ght1pc9kc.baywatch.security.domain.exceptions.UnauthenticatedUser;
import fr.ght1pc9kc.baywatch.security.domain.ports.AuthenticationFacade;
import fr.ght1pc9kc.baywatch.techwatch.domain.model.QueryContext;
import fr.ght1pc9kc.baywatch.techwatch.domain.ports.FeedPersistencePort;
import fr.ght1pc9kc.baywatch.common.infra.mappers.BaywatchMapper;
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
import static org.mockito.Mockito.*;

public class FeedServiceImplTest {
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
        tested = new FeedServiceImpl(mockFeedRepository, mockAuthFacade);
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

        Feed feed = BAYWATCH_MAPPER.recordToFeed(FeedRecordSamples.JEDI);
        StepVerifier.create(tested.persist(List.of(feed)))
                .verifyComplete();

        verify(mockFeedRepository, times(1)).persist(captor.capture(),
                eq(UsersRecordSamples.OKENOBI.getUserId()));
        assertThat(captor.getValue()).containsExactly(feed);
    }
}
