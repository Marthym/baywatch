package fr.ght1pc9kc.baywatch.security.api;

import org.jetbrains.annotations.NotNull;
import reactor.core.publisher.Mono;

public interface PasswordResetService {
    Mono<Void> askPasswordReset(@NotNull String email);

    Mono<Void> resetPassword(@NotNull String token, @NotNull String newPassword);
}
