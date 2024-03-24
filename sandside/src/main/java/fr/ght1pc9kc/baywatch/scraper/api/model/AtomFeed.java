package fr.ght1pc9kc.baywatch.scraper.api.model;

import lombok.Builder;
import org.jetbrains.annotations.Nullable;

import java.net.URI;
import java.time.Instant;

/**
 * <p>Feed forged from the XML Rss or Atom flux. All fields can be null.</p>
 *
 * @param id          Identifies the feed using a universally unique and permanent URI.
 * @param title       Contains a human readable title for the feed.
 * @param description Contains a human-readable description or subtitle for the feed.
 * @param author      Names one author of the feed.
 * @param link        Identifies a related Web page. The type of relation is defined by the rel attribute.
 *                    A feed is limited to one alternate per type and hreflang.
 * @param updated     Indicates the last time the feed was modified in a significant way.
 */
@Builder(toBuilder = true)
public record AtomFeed(
        @Nullable String id,
        @Nullable String title,
        @Nullable String description,
        @Nullable String author,
        @Nullable URI link,
        @Nullable Instant updated
) {
    public static AtomFeed of(String id, URI link) {
        return AtomFeed.builder()
                .id(id).link(link).build();
    }

    public AtomFeed with(String title, String description) {
        return this.toBuilder()
                .title(title)
                .description(description)
                .build();
    }
}
