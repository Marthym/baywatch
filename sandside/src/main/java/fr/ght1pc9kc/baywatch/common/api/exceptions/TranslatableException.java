package fr.ght1pc9kc.baywatch.common.api.exceptions;

public interface TranslatableException {
    String getTranslationKey();

    String getLocalizedMessage();

    String getMessage();

    default String classification() {
        return "INTERNAL_ERROR";
    }
}
