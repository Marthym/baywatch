package fr.ght1pc9kc.baywatch.scraper.domain.actions;

import fr.ght1pc9kc.baywatch.scraper.api.ScrapingErrorsService;
import fr.ght1pc9kc.baywatch.scraper.api.model.AtomEntry;
import fr.ght1pc9kc.baywatch.scraper.api.model.AtomFeed;
import fr.ght1pc9kc.baywatch.scraper.api.model.ScrapResult;
import fr.ght1pc9kc.baywatch.scraper.domain.model.ex.FeedScrapingException;
import fr.ght1pc9kc.baywatch.scraper.domain.model.ex.NewsScrapingException;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.net.URI;
import java.util.List;
import java.util.Set;

import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.assertArg;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class PersistErrorsHandlerTest {

    private final ScrapingErrorsService mockScrapingErrorsService = mock(ScrapingErrorsService.class);
    private PersistErrorsHandler tested;

    @BeforeEach
    void setUp() {
        doAnswer(a -> Flux.fromIterable(a.getArgument(0))).when(mockScrapingErrorsService).persist(anyCollection());
        doReturn(Mono.empty().then()).when(mockScrapingErrorsService).purge(anyCollection());
        tested = new PersistErrorsHandler(mockScrapingErrorsService);
    }

    @Test
    void should_handle_event() {
        Mono<Void> step = tested.after(new ScrapResult(1, List.of(
                new FeedScrapingException(new AtomFeed(
                        "42", "Obiwan Kenobi", null, null,
                        URI.create("https://jedi.com/"), null),
                        new IllegalArgumentException("test")),
                new FeedScrapingException(new AtomFeed(
                        "41", "Obiwan Kenobi", null, null, null, null),
                        new IllegalArgumentException("test")),
                new NewsScrapingException(new AtomEntry(
                        "66", "Kylo Ren", null, null, null,
                        URI.create("https://jedi.com/"), Set.of()),
                        new IllegalArgumentException("test2"))
        )));

        StepVerifier.create(step).verifyComplete();

        verify(mockScrapingErrorsService).persist(assertArg(actual -> Assertions.assertThat(actual).hasSize(1)));
        verify(mockScrapingErrorsService).purge(assertArg(actual -> Assertions.assertThat(actual).hasSize(1)));
    }
}