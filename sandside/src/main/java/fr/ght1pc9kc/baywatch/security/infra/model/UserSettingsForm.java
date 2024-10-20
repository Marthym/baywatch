package fr.ght1pc9kc.baywatch.security.infra.model;

import java.util.Locale;

public record UserSettingsForm(
        Locale preferredLocale,
        boolean autoread
) {
}
