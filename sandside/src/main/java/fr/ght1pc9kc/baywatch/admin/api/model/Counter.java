package fr.ght1pc9kc.baywatch.admin.api.model;

import fr.ght1pc9kc.baywatch.common.api.model.HeroIcons;
import org.jetbrains.annotations.Nullable;

public record Counter(
        String name,
        @Nullable HeroIcons icon,
        String value,
        String description
) {
    public static final Counter NONE = create("None", "N/A", "Empty counter");

    /**
     * @deprecated Use {@link Counter#create(String, String, String)} or {@link Counter#create(String, HeroIcons, String, String)}
     */
    @Deprecated(forRemoval = false)
    @SuppressWarnings("java:S1133")
    public Counter {
        // In order to mask implementation
    }

    public static Counter create(String name, String value, String description) {
        return create(name, null, value, description);
    }

    public static Counter create(String name, HeroIcons icon, String value, String description) {
        return new Counter(name, icon, value, description);
    }
}
