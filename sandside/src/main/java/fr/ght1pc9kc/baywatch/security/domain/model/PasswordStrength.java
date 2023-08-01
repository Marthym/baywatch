package fr.ght1pc9kc.baywatch.security.domain.model;

public record PasswordStrength(
        boolean isSafe,
        double entropy,
        String timeToCrack
) {
}
