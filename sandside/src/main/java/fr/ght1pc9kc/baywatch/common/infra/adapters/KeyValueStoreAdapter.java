package fr.ght1pc9kc.baywatch.common.infra.adapters;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.Expiry;
import fr.ght1pc9kc.baywatch.common.api.KeyValueStore;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Repository;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
public class KeyValueStoreAdapter implements KeyValueStore {

    private final Cache<@NotNull String, ExpirableValue> cache = Caffeine.newBuilder()
            .expireAfter(Expiry.<String, ExpirableValue>writing((k, v) -> v.ttl()))
            .maximumSize(10_000)
            .build();

    @Override
    public Optional<Object> get(String key) {
        return Optional.ofNullable(cache.getIfPresent(key)).map(ExpirableValue::value);
    }

    @Override
    public <T> Optional<T> get(String key, Class<T> type) {
        return Optional.ofNullable(cache.getIfPresent(key)).flatMap(obj -> {
            if (type.isInstance(obj.value())) {
                return Optional.of(type.cast(obj.value()));
            } else {
                return Optional.empty();
            }
        });
    }

    @Override
    public void put(@NotNull String key, @NotNull Object value) {
        cache.put(key, new ExpirableValue(value, ChronoUnit.FOREVER.getDuration()));
    }

    @Override
    public void putAll(Map<String, Object> values) {
        Map<String, ExpirableValue> expirableValueMap = values.entrySet().stream().map(entry ->
                Map.entry(entry.getKey(), new ExpirableValue(entry.getValue(), ChronoUnit.FOREVER.getDuration()))
        ).collect(Collectors.toUnmodifiableMap(Map.Entry::getKey, Map.Entry::getValue));
        cache.putAll(expirableValueMap);
    }

    @Override
    public void put(@NotNull String key, @NotNull Object value, Duration ttl) {
        cache.put(key, new ExpirableValue(value, ttl));
    }

    @Override
    public void putAll(Map<String, Object> values, Duration ttl) {
        Map<String, ExpirableValue> expirableValueMap = values.entrySet().stream().map(entry ->
                Map.entry(entry.getKey(), new ExpirableValue(entry.getValue(), ttl))
        ).collect(Collectors.toUnmodifiableMap(Map.Entry::getKey, Map.Entry::getValue));
        cache.putAll(expirableValueMap);
    }

    @Override
    public Optional<Object> remove(@NotNull String key) {
        return Optional.ofNullable(cache.asMap().remove(key)).map(ExpirableValue::value);
    }

    @Override
    public <T> Optional<T> remove(@NotNull String key, Class<T> type) {
        return Optional.ofNullable(cache.getIfPresent(key)).flatMap(obj -> {
            if (type.isInstance(obj.value())) {
                cache.invalidate(key);
                return Optional.of(type.cast(obj.value()));
            } else {
                return Optional.empty();
            }
        });
    }

    private record ExpirableValue(Object value, Duration ttl) {
    }
}
