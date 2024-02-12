package fr.ght1pc9kc.baywatch.scraper.domain.model;

import java.net.URI;

public record ScrapedFeed(
        String id,
        URI link
) {
    public String shortId() {
        return id.substring(0, 10);
    }
}
