package fr.ght1pc9kc.baywatch.security.api.model;

public record PasswordEvaluation(
        boolean isSecure,
        double entropy,
        String timeToCrack
) {
}
