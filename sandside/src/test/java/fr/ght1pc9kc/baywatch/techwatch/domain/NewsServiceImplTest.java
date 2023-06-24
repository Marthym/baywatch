package fr.ght1pc9kc.baywatch.techwatch.domain;

import fr.ght1pc9kc.baywatch.common.api.model.Entity;
import fr.ght1pc9kc.baywatch.common.domain.exceptions.BadRequestCriteria;
import fr.ght1pc9kc.baywatch.security.api.AuthenticationFacade;
import fr.ght1pc9kc.baywatch.techwatch.api.NewsService;
import fr.ght1pc9kc.baywatch.techwatch.api.model.News;
import fr.ght1pc9kc.baywatch.techwatch.domain.model.QueryContext;
import fr.ght1pc9kc.baywatch.techwatch.domain.ports.FeedPersistencePort;
import fr.ght1pc9kc.baywatch.techwatch.domain.ports.NewsPersistencePort;
import fr.ght1pc9kc.baywatch.techwatch.domain.ports.StatePersistencePort;
import fr.ght1pc9kc.baywatch.techwatch.domain.ports.TeamServicePort;
import fr.ght1pc9kc.baywatch.tests.samples.FeedSamples;
import fr.ght1pc9kc.juery.api.Criteria;
import fr.ght1pc9kc.juery.api.PageRequest;
import fr.ght1pc9kc.juery.basic.filter.ListPropertiesCriteriaVisitor;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static fr.ght1pc9kc.baywatch.common.api.model.EntitiesProperties.FEED_ID;
import static fr.ght1pc9kc.baywatch.common.api.model.EntitiesProperties.NEWS_ID;
import static fr.ght1pc9kc.baywatch.tests.samples.FeedSamples.JEDI;
import static fr.ght1pc9kc.baywatch.tests.samples.FeedSamples.SITH;
import static fr.ght1pc9kc.baywatch.tests.samples.NewsSamples.A_NEW_HOPE;
import static fr.ght1pc9kc.baywatch.tests.samples.NewsSamples.MAY_THE_FORCE;
import static fr.ght1pc9kc.baywatch.tests.samples.NewsSamples.ORDER_66;
import static fr.ght1pc9kc.baywatch.tests.samples.UserSamples.LUKE;
import static fr.ght1pc9kc.baywatch.tests.samples.UserSamples.OBIWAN;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class NewsServiceImplTest {
    private NewsService tested;

    private AuthenticationFacade mockAuthFacade;
    private NewsPersistencePort mockNewsPersistence;
    private TeamServicePort mockTeamServicePort;
    private final ArgumentCaptor<QueryContext> captor = ArgumentCaptor.forClass(QueryContext.class);

    @BeforeEach
    void setUp() {
        mockNewsPersistence = mock(NewsPersistencePort.class);
        when(mockNewsPersistence.list(any(QueryContext.class))).thenReturn(Flux.just(
                MAY_THE_FORCE, ORDER_66, A_NEW_HOPE));
        when(mockNewsPersistence.count(any(QueryContext.class))).thenReturn(Mono.just(3));
        mockAuthFacade = mock(AuthenticationFacade.class);
        StatePersistencePort mockStateRepository = mock(StatePersistencePort.class);
        when(mockStateRepository.list(any())).thenReturn(Flux.just(
                Entity.identify(MAY_THE_FORCE.getId(), OBIWAN.id, MAY_THE_FORCE.getState())
        ));
        FeedPersistencePort mockFeedRepository = mock(FeedPersistencePort.class);
        when(mockFeedRepository.list(any())).thenReturn(Flux.fromIterable(FeedSamples.SAMPLES));

        mockTeamServicePort = mock(TeamServicePort.class);
        when(mockTeamServicePort.getTeamMates(anyString())).thenReturn(Flux.empty());

        tested = new NewsServiceImpl(new ListPropertiesCriteriaVisitor(),
                mockNewsPersistence, mockFeedRepository, mockStateRepository, mockAuthFacade, mockTeamServicePort);
    }

    @Test
    void should_list_news_for_anonymous() {
        when(mockAuthFacade.getConnectedUser()).thenReturn(Mono.empty());
        News actual = tested.list(PageRequest.all()).next().block();

        Assertions.assertThat(actual).isNotNull();
        Assertions.assertThat(actual.getRaw()).isEqualTo(MAY_THE_FORCE.getRaw());
        Assertions.assertThat(actual.getState()).isEqualTo(MAY_THE_FORCE.getState());
    }

    @Test
    void should_list_with_illegal_filters_for_anonymous() {
        when(mockAuthFacade.getConnectedUser()).thenReturn(Mono.empty());
        PageRequest pageRequest = PageRequest.all(
                Criteria.property("title").eq("May the Force")
                        .and(Criteria.property("read").eq(true)));

        StepVerifier.create(tested.list(pageRequest))
                .verifyErrorMatches(t -> t instanceof BadRequestCriteria
                        && t.getMessage().contains("read")
                        && !t.getMessage().contains("title"));
    }

    @Test
    void should_list_with_illegal_filters_for_user() {
        when(mockAuthFacade.getConnectedUser()).thenReturn(Mono.just(LUKE));
        PageRequest pageRequest = PageRequest.all(
                Criteria.property("illegal").eq("May the Force")
                        .and(Criteria.property("read").eq(true)));

        StepVerifier.create(tested.list(pageRequest))
                .verifyErrorMatches(t -> t instanceof BadRequestCriteria
                        && t.getMessage().contains("illegal")
                        && !t.getMessage().contains("read"));
    }

    @Test
    void should_list_news_for_authenticated_user() {
        when(mockAuthFacade.getConnectedUser()).thenReturn(Mono.just(LUKE));

        StepVerifier.create(tested.list(PageRequest.all()))
                .assertNext(actual -> {
                    Assertions.assertThat(actual).isNotNull();
                    Assertions.assertThat(actual.getRaw()).isEqualTo(MAY_THE_FORCE.getRaw());
                    Assertions.assertThat(actual.getState()).isEqualTo(MAY_THE_FORCE.getState());
                })
                .expectNextCount(2)
                .verifyComplete();

        ArgumentCaptor<String> userIdCaptor = ArgumentCaptor.forClass(String.class);
        verify(mockTeamServicePort, times(1)).getTeamMates(userIdCaptor.capture());
        Assertions.assertThat(userIdCaptor.getValue()).isEqualTo(LUKE.id);

        verify(mockNewsPersistence, times(1)).list(captor.capture());
        Assertions.assertThat(captor.getValue().filter).isEqualTo(
                Criteria.or( // FEED_ID in the 2 FEEDS ids plus the ID of the connected user
                        Criteria.property(FEED_ID).in(JEDI.getId(), SITH.getId(), LUKE.id),
                        Criteria.property(NEWS_ID).in(MAY_THE_FORCE.getId())
                )
        );
    }

    @Test
    void should_count_for_authenticated_user() {
        when(mockAuthFacade.getConnectedUser()).thenReturn(Mono.just(LUKE));

        StepVerifier.create(tested.count(PageRequest.all()))
                .assertNext(actual -> {
                    Assertions.assertThat(actual).isNotNull();
                    Assertions.assertThat(actual).isEqualTo(3);
                })
                .verifyComplete();

        ArgumentCaptor<String> userIdCaptor = ArgumentCaptor.forClass(String.class);
        verify(mockTeamServicePort, times(1)).getTeamMates(userIdCaptor.capture());
        Assertions.assertThat(userIdCaptor.getValue()).isEqualTo(LUKE.id);

        verify(mockNewsPersistence, times(1)).count(captor.capture());
        Assertions.assertThat(captor.getValue().filter).isEqualTo(
                Criteria.or(// FEED_ID in the 2 FEEDS ids plus the ID of the connected user
                        Criteria.property(FEED_ID).in(JEDI.getId(), SITH.getId(), LUKE.id),
                        Criteria.property(NEWS_ID).in(MAY_THE_FORCE.getId())
                )
        );
    }

    @Test
    void should_list_news_with_teammates() {
        when(mockAuthFacade.getConnectedUser()).thenReturn(Mono.just(LUKE));
        when(mockTeamServicePort.getTeamMates(LUKE.id)).thenReturn(Flux.just(OBIWAN.id));

        StepVerifier.create(tested.list(PageRequest.all()))
                .assertNext(actual -> {
                    Assertions.assertThat(actual).isNotNull();
                    Assertions.assertThat(actual.getRaw()).isEqualTo(MAY_THE_FORCE.getRaw());
                    Assertions.assertThat(actual.getState()).isEqualTo(MAY_THE_FORCE.getState());
                })
                .expectNextCount(2)
                .verifyComplete();

        ArgumentCaptor<String> userIdCaptor = ArgumentCaptor.forClass(String.class);
        verify(mockTeamServicePort, times(1)).getTeamMates(userIdCaptor.capture());
        Assertions.assertThat(userIdCaptor.getValue()).isEqualTo(LUKE.id);

        verify(mockNewsPersistence, times(1)).list(captor.capture());
        Assertions.assertThat(captor.getValue().filter).isEqualTo(
                Criteria.or( // FEED_ID in the 2 FEEDS ids plus the ID of the connected user
                        Criteria.property(FEED_ID).in(JEDI.getId(), SITH.getId(), LUKE.id),
                        Criteria.property(NEWS_ID).in(MAY_THE_FORCE.getId())
                )
        );
        Assertions.assertThat(captor.getValue().teamMates).containsOnly(LUKE.id, OBIWAN.id);
    }

    @Test
    void should_get_news_for_anonymous() {
        when(mockAuthFacade.getConnectedUser()).thenReturn(Mono.empty());
        News actual = tested.get(MAY_THE_FORCE.getId()).block();

        Assertions.assertThat(actual).isNotNull();
        Assertions.assertThat(actual.getRaw()).isEqualTo(MAY_THE_FORCE.getRaw());
        Assertions.assertThat(actual.getState()).isEqualTo(MAY_THE_FORCE.getState());
    }

    @Test
    void should_get_news_for_authenticated_user() {
        when(mockAuthFacade.getConnectedUser()).thenReturn(Mono.just(LUKE));
        News actual = tested.get(MAY_THE_FORCE.getId()).block();

        Assertions.assertThat(actual).isNotNull();
        Assertions.assertThat(actual.getRaw()).isEqualTo(MAY_THE_FORCE.getRaw());
        Assertions.assertThat(actual.getState()).isEqualTo(MAY_THE_FORCE.getState());
    }
}