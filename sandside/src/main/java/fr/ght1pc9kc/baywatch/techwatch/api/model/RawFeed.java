package fr.ght1pc9kc.baywatch.techwatch.api.model;

import lombok.Builder;
import lombok.NonNull;
import lombok.Value;

import java.net.URI;
import java.time.Instant;

@Value
@Builder
public class RawFeed {
    public @NonNull String id;
    public String name;
    public String description;
    public @NonNull URI url;
    public Instant lastWatch;
}
