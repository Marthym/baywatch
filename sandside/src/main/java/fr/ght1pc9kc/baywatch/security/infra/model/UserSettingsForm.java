package fr.ght1pc9kc.baywatch.security.infra.model;

import fr.ght1pc9kc.baywatch.security.api.model.NewsViewType;

import java.util.Locale;

public record UserSettingsForm(
        Locale preferredLocale,
        boolean autoread,
        NewsViewType newsViewMode
) {
}
