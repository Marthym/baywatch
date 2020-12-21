package fr.ght1pc9kc.baywatch.api.model;

import lombok.Builder;
import lombok.Delegate;
import lombok.NonNull;
import lombok.Value;

import java.util.Set;

@Value
@Builder
public class Feed {
    @Delegate(types = RawFeed.class)
    @NonNull RawFeed raw;
    Set<String> tags;
}
