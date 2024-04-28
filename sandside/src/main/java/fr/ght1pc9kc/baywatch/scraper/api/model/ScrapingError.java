package fr.ght1pc9kc.baywatch.scraper.api.model;

import java.time.Instant;

public record ScrapingError(
        int code,
        Instant since,
        Instant lastTime,
        String message
) {
}
