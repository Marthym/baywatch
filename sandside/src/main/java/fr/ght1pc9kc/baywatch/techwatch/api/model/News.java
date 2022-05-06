package fr.ght1pc9kc.baywatch.techwatch.api.model;

import lombok.Builder;
import lombok.NonNull;
import lombok.Value;
import lombok.With;
import lombok.experimental.Delegate;

import java.util.Set;

/**
 * The News element customized with state and {@link RawFeed#id}
 */
@With
@Value
@Builder
public class News {
    @Delegate(types = RawNews.class)
    @NonNull RawNews raw;

    Set<String> feeds;
    Set<String> tags;

    @Delegate(types = State.class)
    @NonNull State state;
}
