package fr.ght1pc9kc.baywatch.scraper.api.model;

import fr.ght1pc9kc.baywatch.scraper.domain.model.ScrapedFeed;

public record ScrapingError(
        ScrapedFeed feed, Throwable exception
) {
}
