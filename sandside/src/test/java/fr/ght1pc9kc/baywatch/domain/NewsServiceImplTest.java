package fr.ght1pc9kc.baywatch.domain;

import fr.ght1pc9kc.baywatch.api.NewsService;
import fr.ght1pc9kc.baywatch.api.model.Flags;
import fr.ght1pc9kc.baywatch.api.model.News;
import fr.ght1pc9kc.baywatch.api.model.RawNews;
import fr.ght1pc9kc.baywatch.api.model.State;
import fr.ght1pc9kc.baywatch.api.security.model.Role;
import fr.ght1pc9kc.baywatch.api.security.model.User;
import fr.ght1pc9kc.baywatch.domain.exceptions.BadRequestCriteria;
import fr.ght1pc9kc.baywatch.domain.ports.AuthenticationFacade;
import fr.ght1pc9kc.baywatch.domain.techwatch.NewsServiceImpl;
import fr.ght1pc9kc.baywatch.domain.techwatch.ports.NewsPersistencePort;
import fr.ght1pc9kc.juery.api.Criteria;
import fr.ght1pc9kc.juery.api.PageRequest;
import fr.ght1pc9kc.juery.basic.filter.ListPropertiesCriteriaVisitor;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.time.Instant;
import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class NewsServiceImplTest {
    private static final News SAMPLE_NEWS_01 = News.builder()
            .raw(RawNews.builder()
                    .id("0000000000000000000000000000000000000000000000000000000000000001")
                    .title("News 01")
                    .description("Description 01")
                    .link(URI.create("http://blog.ght1pc9kc.fr/news/01"))
                    .publication(Instant.parse("2020-12-30T10:10:01Z"))
                    .build())
            .state(State.of(Flags.READ))
            .feeds(Set.of("1000000000000000000000000000000000000000000000000000000000000000"))
            .build();
    private static final News SAMPLE_NEWS_02 = News.builder()
            .raw(RawNews.builder()
                    .id("0000000000000000000000000000000000000000000000000000000000000002")
                    .title("News 02")
                    .description("Description 02")
                    .link(URI.create("http://blog.ght1pc9kc.fr/news/02"))
                    .publication(Instant.parse("2020-12-30T10:10:01Z"))
                    .build())
            .state(State.of(Flags.SHARED | Flags.READ))
            .feeds(Set.of("1000000000000000000000000000000000000000000000000000000000000000"))
            .build();
    private static final User SAMPLE_USER_01 = User.builder()
            .id("6400659ef2153aa3dacdf921fd9490f39cc681317431d22db274bff220df9eed")
            .login("okenobi")
            .name("Obiwan Kenobi")
            .mail("okenobi@jedi.light")
            .role(Role.USER)
            .build();

    private NewsService tested;

    private AuthenticationFacade mockAuthFacade;

    @BeforeEach
    void setUp() {
        NewsPersistencePort mockNewsPersistence = mock(NewsPersistencePort.class);
        when(mockNewsPersistence.list(any())).thenReturn(Flux.just(
                SAMPLE_NEWS_01, SAMPLE_NEWS_02));
        mockAuthFacade = mock(AuthenticationFacade.class);

        tested = new NewsServiceImpl(new ListPropertiesCriteriaVisitor(), mockNewsPersistence, mockAuthFacade);
    }

    @Test
    void should_list_news_for_anonymous() {
        when(mockAuthFacade.getConnectedUser()).thenReturn(Mono.empty());
        News actual = tested.list(PageRequest.all()).next().block();

        Assertions.assertThat(actual).isNotNull();
        Assertions.assertThat(actual.getRaw()).isEqualTo(SAMPLE_NEWS_01.getRaw());
        Assertions.assertThat(actual.getState()).isEqualTo(SAMPLE_NEWS_01.getState());
    }

    @Test
    void should_list_with_illegal_filters_for_anonymous() {
        when(mockAuthFacade.getConnectedUser()).thenReturn(Mono.empty());

        Assertions.assertThatThrownBy(() -> tested.list(PageRequest.all(
                Criteria.property("title").eq("May the Force")
                        .and(Criteria.property("read").eq(true))
        )).next().block())
                .isInstanceOf(BadRequestCriteria.class)
                .hasMessageNotContaining("title")
                .hasMessageContaining("read");
    }

    @Test
    void should_list_with_illegal_filters_for_user() {
        when(mockAuthFacade.getConnectedUser()).thenReturn(Mono.just(SAMPLE_USER_01));

        Assertions.assertThatThrownBy(() -> tested.list(PageRequest.all(
                Criteria.property("illegal").eq("May the Force")
                        .and(Criteria.property("read").eq(true))
        )).next().block())
                .isInstanceOf(BadRequestCriteria.class)
                .hasMessageNotContaining("read")
                .hasMessageContaining("illegal");
    }

    @Test
    void should_list_news_for_authenticated_user() {
        when(mockAuthFacade.getConnectedUser()).thenReturn(Mono.just(SAMPLE_USER_01));
        News actual = tested.list(PageRequest.all()).next().block();

        Assertions.assertThat(actual).isNotNull();
        Assertions.assertThat(actual.getRaw()).isEqualTo(SAMPLE_NEWS_01.getRaw());
        Assertions.assertThat(actual.getState()).isEqualTo(SAMPLE_NEWS_01.getState());
    }

    @Test
    void should_get_news_for_anonymous() {
        when(mockAuthFacade.getConnectedUser()).thenReturn(Mono.empty());
        News actual = tested.get(SAMPLE_NEWS_01.getId()).block();

        Assertions.assertThat(actual).isNotNull();
        Assertions.assertThat(actual.getRaw()).isEqualTo(SAMPLE_NEWS_01.getRaw());
        Assertions.assertThat(actual.getState()).isEqualTo(SAMPLE_NEWS_01.getState());
    }

    @Test
    void should_get_news_for_authenticated_user() {
        when(mockAuthFacade.getConnectedUser()).thenReturn(Mono.just(SAMPLE_USER_01));
        News actual = tested.get(SAMPLE_NEWS_01.getId()).block();

        Assertions.assertThat(actual).isNotNull();
        Assertions.assertThat(actual.getRaw()).isEqualTo(SAMPLE_NEWS_01.getRaw());
        Assertions.assertThat(actual.getState()).isEqualTo(SAMPLE_NEWS_01.getState());
    }
}