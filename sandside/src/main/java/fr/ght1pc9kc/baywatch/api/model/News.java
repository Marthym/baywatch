package fr.ght1pc9kc.baywatch.api.model;

import lombok.Builder;
import lombok.Delegate;
import lombok.NonNull;
import lombok.Value;

/**
 * The News element customized with state and {@link Feed#id}
 */
@Value
@Builder
public class News {
    @Delegate(types = RawNews.class)
    @NonNull RawNews raw;

    String feedId;

    @Delegate(types = State.class)
    @NonNull State state;
}
