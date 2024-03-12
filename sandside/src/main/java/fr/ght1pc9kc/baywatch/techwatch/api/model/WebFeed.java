package fr.ght1pc9kc.baywatch.techwatch.api.model;

import lombok.Builder;
import lombok.NonNull;
import lombok.With;
import org.jetbrains.annotations.Unmodifiable;

import java.net.URI;
import java.util.Set;

/**
 * The WebFeed entity represents a syndication to an RSS or Atom Flux.
 *
 * @param name        The name of the flux
 * @param description The Description of the flux
 * @param location    The location where the flux is available
 * @param tags        The tags for the flux
 */
@Builder(toBuilder = true)
public record WebFeed(
        @With String name,
        @With String description,
        @NonNull URI location,
        @NonNull @Unmodifiable Set<String> tags
) {
}
