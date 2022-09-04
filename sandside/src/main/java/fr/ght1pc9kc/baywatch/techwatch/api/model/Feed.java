package fr.ght1pc9kc.baywatch.techwatch.api.model;

import lombok.Builder;
import lombok.NonNull;
import lombok.Value;
import lombok.With;
import lombok.experimental.Delegate;

import java.util.Set;

@Value
@Builder
public class Feed {
    @Delegate(types = RawFeed.class)
    @With @NonNull RawFeed raw;
    String name;
    Set<String> tags;
}
