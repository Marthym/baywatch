package fr.ght1pc9kc.baywatch.security.domain.model;

import lombok.experimental.UtilityClass;

import java.util.List;
import java.util.Locale;

@UtilityClass
public class AvailableLanguages {
    public static final Locale ENGLISH = Locale.US;
    public static final Locale FRENCH = Locale.FRANCE;

    public static final Locale DEFAULT = ENGLISH;
    public static final List<Locale> ALL = List.of(ENGLISH, FRENCH);
}
