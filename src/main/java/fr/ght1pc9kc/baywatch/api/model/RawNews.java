package fr.ght1pc9kc.baywatch.api.model;

import lombok.Builder;
import lombok.NonNull;
import lombok.Value;

import java.net.URI;
import java.time.Instant;

/**
 * The News element out of the BDD. Not customized for the user
 */
@Value
@Builder
public class RawNews {
    public @NonNull String id;
    public String title;
    public String description;
    public Instant publication;
    public @NonNull URI link;
}
