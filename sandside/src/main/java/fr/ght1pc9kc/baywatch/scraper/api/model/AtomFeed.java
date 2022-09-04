package fr.ght1pc9kc.baywatch.scraper.api.model;

import java.net.URI;

public record AtomFeed(
        String id,
        String title,
        String description,
        String author,
        URI link
) {
    public AtomFeed with(String title, String description) {
        return new AtomFeed(id(), title, description, author(), link());
    }
}
