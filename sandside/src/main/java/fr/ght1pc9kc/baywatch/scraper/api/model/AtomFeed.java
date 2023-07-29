package fr.ght1pc9kc.baywatch.scraper.api.model;

import org.jetbrains.annotations.Nullable;

import java.net.URI;

public record AtomFeed(
        @Nullable String id,
        @Nullable String title,
        @Nullable String description,
        @Nullable String author,
        @Nullable URI link
) {
    public static AtomFeed of(String id, URI link) {
        return new AtomFeed(id, null, null, null, link);
    }

    public AtomFeed with(String title, String description) {
        return new AtomFeed(id(), title, description, author(), link());
    }
}
