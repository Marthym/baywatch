package fr.ght1pc9kc.baywatch.scraper.infra.adapters;

import fr.ght1pc9kc.baywatch.admin.api.model.Counter;
import fr.ght1pc9kc.baywatch.scraper.domain.actions.ScrapingDurationCounter;
import org.junit.jupiter.api.Test;
import reactor.test.StepVerifier;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;

class ScrapingDurationCounterTest {

    private final ScrapingDurationCounter tested = new ScrapingDurationCounter();

    @Test
    void should_update_counter_as_handler() {
        tested.setClock(Clock.fixed(Instant.parse("2022-04-20T17:18:42.56487Z"), ZoneOffset.UTC));
        StepVerifier.create(tested.before()).verifyComplete();
        tested.setClock(Clock.fixed(Instant.parse("2022-04-20T17:19:24.78465Z"), ZoneOffset.UTC));
        StepVerifier.create(tested.after(42)).verifyComplete();

        StepVerifier.create(tested.computeCounter())
                .expectNext(new Counter("Scraping Duration", "42s 220ms", "Wed, 20 Apr 2022 17:19:24 GMT"))
                .verifyComplete();
    }
}