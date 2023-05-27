package fr.ght1pc9kc.baywatch.scraper.api.model;

public record ScrapingError(
        String link, Throwable exception
) {
}
