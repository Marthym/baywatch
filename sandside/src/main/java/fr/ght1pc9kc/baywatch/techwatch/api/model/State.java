package fr.ght1pc9kc.baywatch.techwatch.api.model;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Value;
import org.intellij.lang.annotations.MagicConstant;

@Value(staticConstructor = "of")
@Getter(AccessLevel.NONE)
public class State {
    public static final State NONE = new State(Flags.NONE);

    //FIXME: Add JsonIgnore in Mixin to avoid sending flags to front
    @MagicConstant(flagsFromClass = Flags.class)
    public int flags;

    public static State of(Integer flags) {
        return (flags != null) ? new State(flags) : NONE;
    }

    public boolean isRead() {
        return (flags & Flags.READ) == Flags.READ;
    }

    public boolean isShared() {
        return (flags & Flags.SHARED) == Flags.SHARED;
    }

    public boolean isKeep() {
        return (flags & Flags.KEEP) == Flags.KEEP;
    }
}
