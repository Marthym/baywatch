package fr.ght1pc9kc.baywatch.scraper.api.model;

import java.time.Instant;

public record ScrapingError(
        Instant since,
        Instant lastTime,
        int status,
        String label
) {
}
