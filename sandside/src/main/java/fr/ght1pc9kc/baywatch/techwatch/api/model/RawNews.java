package fr.ght1pc9kc.baywatch.techwatch.api.model;

import fr.ght1pc9kc.baywatch.scraper.domain.model.Publishable;
import lombok.Builder;
import lombok.NonNull;
import lombok.With;

import java.net.URI;
import java.time.Instant;

/**
 * The News element out of the BDD. Not customized for the user
 */
@With
@Builder(toBuilder = true)
public record RawNews(
        @NonNull String id,
        String title,
        URI image,
        String description,
        Instant publication,
        @NonNull URI link
) {
    public static class RawNewsBuilder implements Publishable<RawNewsBuilder> {
    }
}