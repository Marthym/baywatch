package fr.ght1pc9kc.baywatch.scraper.domain.model;

import org.jetbrains.annotations.Nullable;

import java.net.URI;
import java.time.Instant;

/**
 * <p>The application Feed as scraper module entity.</p>
 *
 * @param id      The sha256 of the normalized feed link
 * @param link    The URL of the feed
 * @param updated The last modification on the feed content
 * @param eTag    The provided ETag if exist
 */
public record ScrapedFeed(
        String id,
        URI link,
        Instant updated,
        @Nullable String eTag
) {
    /**
     * Return the first 10 chars of the feed id
     *
     * @return Feed ID on 10 chars
     */
    public String shortId() {
        return id.substring(0, 10);
    }
}
