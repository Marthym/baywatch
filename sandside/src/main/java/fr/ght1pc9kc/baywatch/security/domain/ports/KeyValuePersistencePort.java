package fr.ght1pc9kc.baywatch.security.domain.ports;

import fr.ght1pc9kc.baywatch.security.api.model.User;
import fr.ght1pc9kc.entity.api.Entity;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;
import java.util.Optional;

public interface KeyValuePersistencePort {
    Optional<Entity<User>> get(@NotNull String hashedToken);

    void store(@NotNull String hashedToken, @NotNull Entity<User> user, @NotNull Duration ttl);

    void remove(@NotNull String hashedToken);
}
