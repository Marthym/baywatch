package fr.ght1pc9kc.baywatch.security.infra.adapters;

import fr.ght1pc9kc.baywatch.common.api.KeyValueStore;
import fr.ght1pc9kc.baywatch.security.api.model.User;
import fr.ght1pc9kc.baywatch.security.domain.ports.KeyValuePersistencePort;
import fr.ght1pc9kc.entity.api.Entity;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Repository;

import java.time.Duration;
import java.util.Optional;

import static java.util.Objects.requireNonNull;

@Repository
@RequiredArgsConstructor
public class KeyValuePersistenceStoreAdapter implements KeyValuePersistencePort {
    private final KeyValueStore kvStore;

    @Override
    @SuppressWarnings("unchecked")
    public Optional<Entity<User>> get(@NonNull String hashedToken) {
        return kvStore.get(requireNonNull(hashedToken))
                .flatMap(obj -> {
                    if (obj instanceof Entity<?> entity && entity.self() instanceof User) {
                        return Optional.of((Entity<User>) entity);
                    }
                    kvStore.remove(hashedToken);
                    return Optional.empty();
                });
    }

    @Override
    public void store(@NonNull String hashedToken, @NonNull Entity<User> user, @NonNull Duration ttl) {
        kvStore.put(requireNonNull(hashedToken),
                requireNonNull(user), requireNonNull(ttl, "ttl must not be null"));
    }

    @Override
    public void remove(@NonNull String hashedToken) {
        kvStore.remove(requireNonNull(hashedToken));
    }
}
