package fr.ght1pc9kc.baywatch.techwatch.domain;

import fr.ght1pc9kc.baywatch.common.infra.mappers.BaywatchMapper;
import fr.ght1pc9kc.baywatch.security.api.AuthenticationFacade;
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
import fr.ght1pc9kc.juery.basic.filter.ListPropertiesCriteriaVisitor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import org.mockito.ArgumentCaptor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.anyString;
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

    @SuppressWarnings("ReactiveStreamsUnusedPublisher")
    @BeforeEach
    void setUp() {
        Feed jediFeed = BAYWATCH_MAPPER.recordToFeed(FeedRecordSamples.JEDI);
        when(mockFeedRepository.get(any())).thenReturn(Mono.just(jediFeed));
        when(mockFeedRepository.list(any())).thenReturn(Flux.just(jediFeed));
        when(mockFeedRepository.persist(any())).thenAnswer(a -> Flux.fromIterable(a.getArgument(0, List.class)));
        when(mockFeedRepository.persistUserRelation(anyCollection(), anyString())).thenAnswer(a -> Flux.fromIterable(a.getArgument(0, List.class)));
        when(mockFeedRepository.count(any())).thenReturn(Mono.just(42));

        ScraperServicePort mockScraperService = mock(ScraperServicePort.class);
        when(mockScraperService.fetchFeedData(any())).thenReturn(Mono.just(FeedSamples.JEDI));
        tested = new FeedServiceImpl(mockFeedRepository, mockScraperService, mockAuthFacade, new ListPropertiesCriteriaVisitor() {
        });
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
        when(mockAuthFacade.getConnectedUser()).thenReturn(Mono.just(UserSamples.OBIWAN));
        ArgumentCaptor<QueryContext> captor = ArgumentCaptor.forClass(QueryContext.class);

        {
            tested.list().collectList().block();

            verify(mockFeedRepository, times(1)).list(captor.capture());
            assertThat(captor.getValue()).isEqualTo(QueryContext.empty().withUserId(UserSamples.OBIWAN.id));
        }

        {
            clearInvocations(mockFeedRepository);
            tested.list(PageRequest.one(Criteria.property("name").eq("jedi"))).collectList().block();

            verify(mockFeedRepository, times(1)).list(captor.capture());
            assertThat(captor.getValue()).isEqualTo(QueryContext.first(
                    Criteria.property("name").eq("jedi")).withUserId(UserSamples.OBIWAN.id));
        }
    }

    @Test
    void should_add_feed_by_anonymous() {
        when(mockAuthFacade.getConnectedUser()).thenReturn(Mono.empty());
        StepVerifier.create(tested.add(List.of())).verifyError(UnauthenticatedUser.class);
    }

    @Test
    void should_subscribe_feeds_for_anonymous() {
        when(mockAuthFacade.getConnectedUser()).thenReturn(Mono.empty());
        StepVerifier.create(tested.subscribe(List.of())).verifyError(UnauthenticatedUser.class);
    }

    @Test
    @SuppressWarnings("unchecked")
    void should_add_feed_from_user() {
        when(mockAuthFacade.getConnectedUser()).thenReturn(Mono.just(UserSamples.OBIWAN));
        ArgumentCaptor<List<Feed>> captor = ArgumentCaptor.forClass(List.class);

        StepVerifier.create(tested.add(List.of(FeedSamples.JEDI)))
                .expectNext(FeedSamples.JEDI)
                .verifyComplete();

        verify(mockFeedRepository, times(1)).persist(captor.capture());
        assertThat(captor.getValue()).containsExactly(FeedSamples.JEDI);
    }

    @Test
    @SuppressWarnings("unchecked")
    void should_subscribe_feeds_for_user() {
        Feed jediFeed = BAYWATCH_MAPPER.recordToFeed(FeedRecordSamples.JEDI);
        when(mockAuthFacade.getConnectedUser()).thenReturn(Mono.just(UserSamples.OBIWAN));
        ArgumentCaptor<List<Feed>> captor = ArgumentCaptor.forClass(List.class);

        StepVerifier.create(tested.subscribe(List.of(jediFeed)))
                .expectNext(jediFeed)
                .verifyComplete();

        verify(mockFeedRepository, times(1)).persistUserRelation(captor.capture(),
                eq(UsersRecordSamples.OKENOBI.getUserId()));
        assertThat(captor.getValue()).containsExactly(jediFeed);
    }

    @Test
    void should_add_unsecured_url() {
        when(mockAuthFacade.getConnectedUser()).thenReturn(Mono.just(UserSamples.OBIWAN));
        StepVerifier.create(tested.add(List.of(FeedSamples.UNSECURE_PROTOCOL)))
                .verifyError(IllegalArgumentException.class);

        StepVerifier.create(tested.update(FeedSamples.UNSECURE_PROTOCOL))
                .verifyError(IllegalArgumentException.class);

    }
}
