package fr.ght1pc9kc.baywatch.techwatch.domain;

import fr.ght1pc9kc.baywatch.common.api.exceptions.UnauthorizedException;
import fr.ght1pc9kc.baywatch.common.infra.mappers.BaywatchMapper;
import fr.ght1pc9kc.baywatch.security.api.AuthenticationFacade;
import fr.ght1pc9kc.baywatch.techwatch.api.SystemMaintenanceService;
import fr.ght1pc9kc.baywatch.techwatch.api.model.WebFeed;
import fr.ght1pc9kc.baywatch.techwatch.domain.model.QueryContext;
import fr.ght1pc9kc.baywatch.techwatch.domain.ports.FeedPersistencePort;
import fr.ght1pc9kc.baywatch.techwatch.domain.ports.NewsPersistencePort;
import fr.ght1pc9kc.baywatch.techwatch.infra.model.FeedDeletedResult;
import fr.ght1pc9kc.baywatch.tests.samples.UserSamples;
import fr.ght1pc9kc.baywatch.tests.samples.infra.FeedRecordSamples;
import fr.ght1pc9kc.entity.api.Entity;
import fr.ght1pc9kc.juery.api.Criteria;
import fr.ght1pc9kc.juery.api.PageRequest;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import org.mockito.ArgumentCaptor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.List;

import static fr.ght1pc9kc.baywatch.common.api.model.EntitiesProperties.FEED_ID;
import static fr.ght1pc9kc.baywatch.common.api.model.EntitiesProperties.NEWS_ID;
import static fr.ght1pc9kc.baywatch.tests.samples.FeedSamples.JEDI;
import static fr.ght1pc9kc.baywatch.tests.samples.FeedSamples.SITH;
import static fr.ght1pc9kc.baywatch.tests.samples.NewsSamples.A_NEW_HOPE;
import static fr.ght1pc9kc.baywatch.tests.samples.NewsSamples.MAY_THE_FORCE;
import static fr.ght1pc9kc.baywatch.tests.samples.UserSamples.LUKE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.Mockito.clearInvocations;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class SystemMaintenanceServiceImplTest {
    public static final BaywatchMapper BAYWATCH_MAPPER = Mappers.getMapper(BaywatchMapper.class);
    private final FeedPersistencePort mockFeedRepository = mock(FeedPersistencePort.class);
    private final NewsPersistencePort mockNewsRepository = mock(NewsPersistencePort.class);
    private final AuthenticationFacade mockAuthFacade = mock(AuthenticationFacade.class);

    private SystemMaintenanceService tested;

    @BeforeEach
    void setUp() {
        tested = new SystemMaintenanceServiceImpl(mockFeedRepository, mockNewsRepository, mockAuthFacade);
        Entity<WebFeed> jediFeed = BAYWATCH_MAPPER.recordToFeed(FeedRecordSamples.JEDI);
        when(mockFeedRepository.get(any())).thenReturn(Mono.just(jediFeed));
        when(mockFeedRepository.list(any())).thenReturn(Flux.just(jediFeed));
        when(mockFeedRepository.delete(any())).thenReturn(Mono.just(new FeedDeletedResult(1, 2)));

        when(mockNewsRepository.delete(anyCollection())).thenReturn(Mono.just(3));
        when(mockNewsRepository.list(any())).thenReturn(Flux.just(MAY_THE_FORCE, A_NEW_HOPE));

        when(mockAuthFacade.getConnectedUser()).thenReturn(Mono.just(UserSamples.THE_FORCE));
    }

    @Test
    void should_list_feed_for_anonymous() {
        reset(mockAuthFacade);
        when(mockAuthFacade.getConnectedUser()).thenReturn(Mono.empty());
        StepVerifier.create(tested.feedList()).verifyError(UnauthorizedException.class);
    }

    @Test
    void should_list_feed_for_user() {
        reset(mockAuthFacade);
        when(mockAuthFacade.getConnectedUser()).thenReturn(Mono.just(LUKE));
        StepVerifier.create(tested.feedList()).verifyError(UnauthorizedException.class);
    }

    @Test
    void should_list_feed_for_admin() {
        ArgumentCaptor<QueryContext> captor = ArgumentCaptor.forClass(QueryContext.class);

        {
            StepVerifier.create(tested.feedList())
                    .expectNextMatches(r -> r.id().equals(FeedRecordSamples.JEDI.getFeedId()))
                    .verifyComplete();

            verify(mockFeedRepository, times(1)).list(captor.capture());
            assertThat(captor.getValue()).isEqualTo(QueryContext.empty());
        }

        {
            clearInvocations(mockFeedRepository);
            StepVerifier.create(tested.feedList(PageRequest.one(Criteria.property("name").eq("jedi"))))
                    .expectNextMatches(r -> r.id().equals(FeedRecordSamples.JEDI.getFeedId()))
                    .verifyComplete();

            verify(mockFeedRepository, times(1)).list(captor.capture());
            assertThat(captor.getValue()).isEqualTo(QueryContext.first(
                    Criteria.property("name").eq("jedi")));
        }
    }

    @Test
    void should_delete_feed_for_user() {
        reset(mockAuthFacade);
        when(mockAuthFacade.getConnectedUser()).thenReturn(Mono.just(LUKE));
        StepVerifier.create(tested.feedDelete(List.of(FeedRecordSamples.JEDI.getFeedId())))
                .verifyError(UnauthorizedException.class);
    }

    @Test
    void should_delete_feed_for_admin() {
        StepVerifier.create(tested.feedDelete(List.of(FeedRecordSamples.JEDI.getFeedId())))
                .expectNext(2)
                .verifyComplete();

        verify(mockFeedRepository, times(1)).delete(any());
    }

    @Test
    void should_list_news_for_authenticated_user() {
        Criteria filter = Criteria.or(
                Criteria.property(FEED_ID).in(JEDI.id(), SITH.id()),
                Criteria.property(NEWS_ID).in(MAY_THE_FORCE.id())
        );
        StepVerifier.create(tested.newsList(PageRequest.all(filter)))
                .assertNext(actual -> {
                    Assertions.assertThat(actual).isNotNull();
                    Assertions.assertThat(actual).isEqualTo(MAY_THE_FORCE);
                })
                .expectNextCount(1)
                .verifyComplete();

        ArgumentCaptor<QueryContext> captor = ArgumentCaptor.forClass(QueryContext.class);
        verify(mockNewsRepository, times(1)).list(captor.capture());
        Assertions.assertThat(captor.getValue().filter).isEqualTo(filter);
    }

    @Test
    @SuppressWarnings("unchecked")
    void should_batch_delete_news() {
        StepVerifier.create(tested.newsDelete(List.of("1", "2", "3")))
                .expectNext(3)
                .verifyComplete();

        ArgumentCaptor<List<String>> captor = ArgumentCaptor.forClass(List.class);
        verify(mockNewsRepository, times(1)).delete(captor.capture());
        assertThat(captor.getValue()).containsExactly("1", "2", "3");
    }
}