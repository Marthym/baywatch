package fr.ght1pc9kc.baywatch.admin.api.model;

import lombok.Builder;
import org.jetbrains.annotations.Nullable;

import java.net.URI;
import java.time.Instant;

@Builder(toBuilder = true)
public record RawFeed(
        String name,
        String description,
        URI url,
        URI icon,
        @Nullable Instant lastWatch,
        @Nullable String lastETag
) {
}
