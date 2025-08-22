package fr.ght1pc9kc.baywatch.security.domain.ports;

import fr.ght1pc9kc.baywatch.security.api.model.User;
import fr.ght1pc9kc.entity.api.Entity;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public interface ResetPasswordTokenPort {
    Optional<Entity<User>> get(@NotNull String hashedToken);

    void store(@NotNull String hashedToken, @NotNull Entity<User> user);

    void remove(@NotNull String hashedToken);
}
