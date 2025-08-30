package fr.ght1pc9kc.baywatch.common.api.exceptions;

import java.util.Map;

public interface TranslatableException {
    String getTranslationKey();

    String getLocalizedMessage();

    String getMessage();

    default String classification() {
        return "INTERNAL_ERROR";
    }

    default Map<String, Object> getExtensions() {
        return Map.of();
    }
}
