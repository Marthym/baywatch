package fr.ght1pc9kc.baywatch.security.api;

import fr.ght1pc9kc.baywatch.security.api.model.PasswordEvaluation;
import fr.ght1pc9kc.baywatch.security.api.model.User;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface PasswordService {
    /**
     * <p>Check the password strength and return an evaluation</p>
     * <p>The current authenticated user is used to populate the password evaluation context</p>
     *
     * @param password The new password
     * @return A password evaluation
     * @throws fr.ght1pc9kc.baywatch.common.api.exceptions.UnauthorizedException When no user was authenticated
     */
    Mono<PasswordEvaluation> checkPasswordStrength(String password);

    /**
     * Check the password strength and return a {@link PasswordEvaluation}
     *
     * @param user The user owner of the password as the context
     * @return A password evaluation
     */
    Mono<PasswordEvaluation> checkPasswordStrength(User user);

    Flux<String> generateSecurePassword(int number);
}
