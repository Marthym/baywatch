package fr.ght1pc9kc.baywatch.techwatch.api.model;

import lombok.Builder;
import lombok.NonNull;
import lombok.With;

import java.net.URI;
import java.time.Instant;

@Deprecated
@Builder
public record RawFeed(
        @NonNull String id,
        @With String name,
        @With String description,
        @NonNull URI url,
        Instant lastWatch
) {
}
