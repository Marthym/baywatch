package fr.ght1pc9kc.baywatch.api.model;

import lombok.Builder;
import lombok.NonNull;
import lombok.Value;

import java.net.URI;
import java.time.Instant;

@Value
@Builder
public class News {
    public @NonNull String id;
    public Integer feedId;
    public String title;
    public String description;
    public Instant publication;
    public @NonNull URI link;
    public boolean stared;
}
