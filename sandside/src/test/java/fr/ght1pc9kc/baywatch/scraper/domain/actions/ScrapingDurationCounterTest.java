package fr.ght1pc9kc.baywatch.scraper.domain.actions;

import fr.ght1pc9kc.baywatch.admin.api.model.Counter;
import fr.ght1pc9kc.baywatch.common.api.model.HeroIcons;
import fr.ght1pc9kc.baywatch.scraper.api.model.ScrapResult;
import org.junit.jupiter.api.Test;
import reactor.test.StepVerifier;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;

class ScrapingDurationCounterTest {

    private final ScrapingDurationCounter tested = new ScrapingDurationCounter();

    @Test
    void should_update_counter_as_handler() {
        tested.setClock(Clock.fixed(Instant.parse("2022-04-20T17:18:42.56487Z"), ZoneOffset.UTC));
        StepVerifier.create(tested.before()).verifyComplete();
        tested.setClock(Clock.fixed(Instant.parse("2022-04-20T17:19:24.78465Z"), ZoneOffset.UTC));
        StepVerifier.create(tested.after(new ScrapResult(42, List.of()))).verifyComplete();
        tested.onTerminate();

        StepVerifier.create(tested.computeCounter())
                .expectNext(Counter.create("Scraping Duration", HeroIcons.CLOUD_ARROWUP_ICON, "42s 220ms", "2022-04-20T17:19:24.784650Z"))
                .verifyComplete();
    }
}