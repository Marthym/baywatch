package fr.ght1pc9kc.baywatch.security.api.model;

import java.util.Locale;

public record UserSettings(
        Locale preferredLocale,
        boolean autoread
) {
}
