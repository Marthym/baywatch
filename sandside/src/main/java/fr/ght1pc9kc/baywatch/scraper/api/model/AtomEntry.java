package fr.ght1pc9kc.baywatch.scraper.api.model;

import lombok.NonNull;

import java.net.URI;
import java.time.Instant;
import java.util.Set;

public record AtomEntry(
        @NonNull String id,
        String title,
        URI image,
        String description,
        Instant publication,
        @NonNull URI link,
        Set<String> feeds
) {
}
