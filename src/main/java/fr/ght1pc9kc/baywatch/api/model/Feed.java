package fr.ght1pc9kc.baywatch.api.model;

import lombok.Builder;
import lombok.NonNull;
import lombok.Value;

import java.net.URI;
import java.time.Instant;
import java.util.Collection;

@Value
@Builder
public class Feed {
    public @NonNull String id;
    public String name;
    public @NonNull URI url;
    public Instant lastWatch;
    public Collection<Folder> folders;
}
