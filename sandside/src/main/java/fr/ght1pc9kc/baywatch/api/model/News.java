package fr.ght1pc9kc.baywatch.api.model;

import lombok.Builder;
import lombok.NonNull;
import lombok.Value;
import lombok.With;
import lombok.experimental.Delegate;

import java.net.URI;

/**
 * The News element customized with state and {@link RawFeed#id}
 */
@With
@Value
@Builder
public class News {
    @Delegate(types = RawNews.class)
    @NonNull RawNews raw;

    String feedId;

    @Delegate(types = State.class)
    @NonNull State state;
}
