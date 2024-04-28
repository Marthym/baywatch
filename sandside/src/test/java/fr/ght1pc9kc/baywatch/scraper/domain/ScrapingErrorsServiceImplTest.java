package fr.ght1pc9kc.baywatch.scraper.domain;

import fr.ght1pc9kc.baywatch.common.domain.QueryContext;
import fr.ght1pc9kc.baywatch.scraper.api.model.ScrapingError;
import fr.ght1pc9kc.baywatch.scraper.domain.ports.ScrapingAuthentFacade;
import fr.ght1pc9kc.baywatch.scraper.domain.ports.ScrapingErrorPersistencePort;
import fr.ght1pc9kc.baywatch.tests.samples.FeedSamples;
import fr.ght1pc9kc.baywatch.tests.samples.UserSamples;
import fr.ght1pc9kc.entity.api.Entity;
import fr.ght1pc9kc.juery.api.Criteria;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.Instant;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.assertArg;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class ScrapingErrorsServiceImplTest {

    private final ScrapingErrorPersistencePort persistencePort = mock(ScrapingErrorPersistencePort.class);
    private final ScrapingAuthentFacade authentFacade = mock(ScrapingAuthentFacade.class);
    private ScrapingErrorsServiceImpl tested;

    @BeforeEach
    void setUp() {
        doReturn(Mono.just(UserSamples.OBIWAN)).when(authentFacade).getConnectedUser();
        doReturn(false).when(authentFacade).hasSystemRole(any());
        doAnswer(a -> Flux.fromIterable(a.getArgument(0))).when(persistencePort).persist(anyCollection());
        doReturn(Mono.empty().then()).when(persistencePort).delete(any());

        tested = new ScrapingErrorsServiceImpl(persistencePort, authentFacade);
    }

    @Test
    void should_fail_to_persist() {
        Instant since = Instant.parse("2024-04-02T22:08:42Z");
        Flux<Entity<ScrapingError>> step = tested.persist(List.of(
                Entity.identify(new ScrapingError(403, since, since, "Not found"))
                        .withId(FeedSamples.JEDI.id()),
                Entity.identify(new ScrapingError(403, since, since, "Not found"))
                        .withId(FeedSamples.SITH.id())
        ));

        StepVerifier.create(step)
                .verifyError(IllegalAccessException.class);
    }

    @Test
    void should_persist() {
        doReturn(Mono.just(UserSamples.THE_FORCE)).when(authentFacade).getConnectedUser();
        doReturn(true).when(authentFacade).hasSystemRole(any());

        Instant since = Instant.parse("2024-04-02T22:08:42Z");
        Flux<Entity<ScrapingError>> step = tested.persist(List.of(
                Entity.identify(new ScrapingError(404, since, since, "Not found"))
                        .withId(FeedSamples.JEDI.id()),
                Entity.identify(new ScrapingError(404, since, since, "Not found"))
                        .withId(FeedSamples.SITH.id())
        ));

        StepVerifier.create(step)
                .expectNextCount(2)
                .verifyComplete();

        verify(persistencePort).persist(anyCollection());
    }

    @Test
    void should_fail_to_purge() {
        Mono<Void> step = tested.purge(List.of("42", "43"));

        StepVerifier.create(step)
                .verifyError(IllegalAccessException.class);
    }

    @Test
    void should_purge() {
        doReturn(Mono.just(UserSamples.THE_FORCE)).when(authentFacade).getConnectedUser();
        doReturn(true).when(authentFacade).hasSystemRole(any());

        Mono<Void> step = tested.purge(List.of("42", "43"));

        StepVerifier.create(step)
                .verifyComplete();

        verify(persistencePort).delete(assertArg(actual -> Assertions.assertThat(actual).isEqualTo(
                QueryContext.all(Criteria.not(Criteria.property("id").in("42", "43"))))));
    }

    @Test
    void should_list() {
        Instant since = Instant.parse("2024-04-02T22:08:42Z");
        doReturn(Flux.just(
                Entity.identify(new ScrapingError(404, since, since, "Not found"))
                        .withId(FeedSamples.JEDI.id()),
                Entity.identify(new ScrapingError(404, since, since, "Not found"))
                        .withId(FeedSamples.SITH.id())
        )).when(persistencePort).list(any());

        StepVerifier.create(tested.list(List.of("42", "43")))
                .expectNextCount(2)
                .verifyComplete();

        verify(persistencePort).list(assertArg(actual -> Assertions.assertThat(actual).isEqualTo(
                QueryContext.all(Criteria.property("id").in("42", "43")))));
    }
}