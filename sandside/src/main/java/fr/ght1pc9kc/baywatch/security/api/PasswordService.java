package fr.ght1pc9kc.baywatch.security.api;

import fr.ght1pc9kc.baywatch.security.api.model.PasswordEvaluation;
import reactor.core.publisher.Mono;

public interface PasswordService {
    /**
     * Check the password strength and return an evaluation
     *
     * @param password The new password
     * @return {@code Void} when the password is changed
     * @throws IllegalArgumentException When password strength is not enough
     */
    Mono<PasswordEvaluation> checkPasswordStrength(String password);

    Mono<String> generateSecurePassword();
}
