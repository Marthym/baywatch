package fr.ght1pc9kc.baywatch.techwatch.api.model;

import lombok.Builder;
import lombok.NonNull;
import lombok.Value;
import lombok.With;

import java.net.URI;
import java.time.Instant;

@Value
@Builder
public class RawFeed {
    public @NonNull String id;
    public @With String name;
    public @With String description;
    public @NonNull URI url;
    public Instant lastWatch;
}
