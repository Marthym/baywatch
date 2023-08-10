package fr.ght1pc9kc.baywatch.techwatch.api.model;

import lombok.Builder;
import lombok.NonNull;
import lombok.Value;
import lombok.With;

import java.net.URI;
import java.time.Instant;
import java.util.Optional;
import java.util.Set;

@Value
@Builder
public class Feed {
    @With
    @NonNull RawFeed raw;
    String name;
    Set<String> tags;

    public String getName() {
        return Optional.ofNullable(this.name).orElse(raw.name());
    }

    public String getId() {
        return raw.id();
    }

    public String getDescription() {
        return raw.description();
    }

    public URI getUrl() {
        return raw.url();
    }

    public Instant getLastWatch() {
        return raw.lastWatch();
    }
}
