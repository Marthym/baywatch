package fr.ght1pc9kc.baywatch.scraper.infra.controllers;

import fr.ght1pc9kc.baywatch.scraper.api.ScrapingErrorsService;
import fr.ght1pc9kc.baywatch.scraper.api.model.ScrapingError;
import fr.ght1pc9kc.entity.api.Entity;
import org.assertj.core.api.Assertions;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

class ScrapingErrorsControllerTest {
    private ScrapingErrorsController tested;

    private ScrapingErrorsService mockScrapingErrorService;

    @BeforeEach
    void setUp() {
        mockScrapingErrorService = mock(ScrapingErrorsService.class);
        tested = new ScrapingErrorsController(mockScrapingErrorService);
    }

    @Test
    void should_get_errors_from_feeds() {
        Instant since = Instant.parse("2024-05-05T12:42:02Z");
        ScrapingError expected = new ScrapingError(404, since, since, "Not found");
        doReturn(Flux.just(
                Entity.identify(expected)
                        .withId("f9e2eaaa42d9fe9e558a9b8ef1bf366f190aacaa83bad2641ee106e9041096e4")
        )).when(mockScrapingErrorService).list(anyCollection());

        StepVerifier.create(tested.errors(List.of(Map.of("_id", "f9e2eaaa42d9fe9e558a9b8ef1bf366f190aacaa83bad2641ee106e9041096e4"))))
                .assertNext(actual -> SoftAssertions.assertSoftly(softly -> {
                    softly.assertThat(actual).isNotNull().hasSize(1);
                    softly.assertThat(actual.values().stream().findFirst()).contains(expected);
                }))
                .verifyComplete();

        StepVerifier.create(tested.errors(List.of()))
                .assertNext(actual -> Assertions.assertThat(actual).isNotNull().isEmpty())
                .verifyComplete();
    }

    @Test
    void should_compute_error_level() {
        doReturn(Level.WARNING).when(mockScrapingErrorService).level(any(ScrapingError.class));
        Instant since = Instant.parse("2024-05-05T12:42:02Z");
        ScrapingError sample = new ScrapingError(404, since, since, "Not found");

        StepVerifier.create(tested.computeErrorLevel(sample))
                .assertNext(actual -> Assertions.assertThat(actual).isEqualTo(Level.WARNING.toString()))
                .verifyComplete();
    }

    @Test
    void should_filter_error_message() {
        doReturn("Filtered").when(mockScrapingErrorService).filterMessage(any(ScrapingError.class));
        Instant since = Instant.parse("2024-05-05T12:42:02Z");
        ScrapingError sample = new ScrapingError(404, since, since, "Not found");

        StepVerifier.create(tested.filterErrorMessage(sample))
                .assertNext(actual -> Assertions.assertThat(actual).isEqualTo("Filtered"))
                .verifyComplete();
    }
}