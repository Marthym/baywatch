package fr.ght1pc9kc.baywatch.api.model;

import lombok.Builder;
import lombok.Delegate;
import lombok.NonNull;
import lombok.Value;
import org.intellij.lang.annotations.MagicConstant;

/**
 * The News element customized with state and {@link Feed#id}
 */
@Value
@Builder
public class News {
    @Delegate(types = RawNews.class)
    @NonNull RawNews raw;

    Integer feedId;

    @MagicConstant(flagsFromClass = Flags.class)
    int state;

    public boolean isRead() {
        return (state & Flags.READ) != 0;
    }

    public boolean isStared() {
        return (state & Flags.STAR) != 0;
    }
}
