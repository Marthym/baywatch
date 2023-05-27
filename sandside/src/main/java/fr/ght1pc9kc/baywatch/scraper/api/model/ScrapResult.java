package fr.ght1pc9kc.baywatch.scraper.api.model;

import java.util.List;

public record ScrapResult(
        long inserted,
        List<ScrapingError> errors
) {
}
