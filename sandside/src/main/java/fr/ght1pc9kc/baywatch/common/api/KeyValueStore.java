package fr.ght1pc9kc.baywatch.common.api;

import org.jetbrains.annotations.NotNull;

import java.time.Duration;
import java.util.Map;
import java.util.Optional;

public interface KeyValueStore {
    Optional<Object> get(String key);

    <T> Optional<T> get(String key, Class<T> type);

    void put(@NotNull String key, @NotNull Object value);

    void putAll(Map<@NotNull String, @NotNull Object> values);

    void put(@NotNull String key, @NotNull Object value, Duration ttl);

    void putAll(Map<@NotNull String, @NotNull Object> values, Duration ttl);

    Optional<Object> remove(@NotNull String key);

    <T> Optional<T> remove(@NotNull String key, Class<T> type);
}
