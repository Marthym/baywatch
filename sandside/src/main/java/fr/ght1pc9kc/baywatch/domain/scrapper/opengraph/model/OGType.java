package fr.ght1pc9kc.baywatch.domain.scrapper.opengraph.model;

import org.jetbrains.annotations.NotNull;

import java.util.NoSuchElementException;

public enum OGType {
    ARTICLE, BLOG, BOOK, PROFILE, WEBSITE, VIDEO, VIDEO_OTHER, MUSIC, OBJECT;

    private static final OGType[] ALL = values();

    public static OGType from(@NotNull String value) {
        if (value.isBlank()) {
            throw new IllegalArgumentException();
        }
        String normalized = value.replace('.', '_').toUpperCase();
        for (OGType ogType : ALL) {
            if (ogType.name().equals(normalized)) {
                return ogType;
            }
        }
        throw new NoSuchElementException(String.format("OGType for %s not found !", value));
    }
}
