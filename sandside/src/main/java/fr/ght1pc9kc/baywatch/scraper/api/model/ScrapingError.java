package fr.ght1pc9kc.baywatch.scraper.api.model;

import fr.ght1pc9kc.baywatch.scraper.domain.model.ex.ScrapingExceptionCode;

import java.time.Instant;

public record ScrapingError(
        ScrapingExceptionCode code,
        Instant since,
        Instant lastTime
) {
}
