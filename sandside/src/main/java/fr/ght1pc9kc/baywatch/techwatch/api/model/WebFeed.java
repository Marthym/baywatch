package fr.ght1pc9kc.baywatch.techwatch.api.model;

import lombok.Builder;
import lombok.NonNull;
import lombok.With;
import org.jetbrains.annotations.Unmodifiable;

import java.net.URI;
import java.time.Instant;
import java.util.Set;

/**
 * The WebFeed entity represent a syndication to ans RSS or Atom Flux.
 *
 * @param reference   The sha256 of the location
 * @param name        The name of the flux
 * @param description The Description of the flux
 * @param location    The location where the flux is available
 * @param tags        The tags for the flux
 * @param updated     The last feed content modification date
 */
@Builder(toBuilder = true)
public record WebFeed(
        @NonNull String reference,
        @With String name,
        @With String description,
        @NonNull URI location,
        @NonNull @Unmodifiable Set<String> tags,
        @NonNull Instant updated
) {
}
