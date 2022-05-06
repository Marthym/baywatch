package fr.ght1pc9kc.baywatch.techwatch.api.model;

import lombok.Builder;
import lombok.NonNull;
import lombok.Value;
import lombok.With;

import java.net.URI;
import java.time.Instant;

/**
 * The News element out of the BDD. Not customized for the user
 */
@With
@Value
@Builder(toBuilder = true)
public class RawNews {
    public @NonNull String id;
    public String title;
    public URI image;
    public String description;
    public Instant publication;
    public @NonNull URI link;
}
