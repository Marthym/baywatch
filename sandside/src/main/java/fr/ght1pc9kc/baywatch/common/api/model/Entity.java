package fr.ght1pc9kc.baywatch.common.api.model;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import org.jetbrains.annotations.NotNull;

import java.time.Instant;

/**
 * Hold the standard Persistence information
 *
 * @param <T> The type of the persisted object
 */
@Builder
@ToString
@EqualsAndHashCode
@RequiredArgsConstructor
public final class Entity<T> {
    public static final String IDENTIFIER = "_id";
    public static final String CREATED_AT = "_createdAt";
    public static final String CREATED_BY = "_createdBy";
    public static final String NO_ONE = "_";

    public final @NonNull String id;
    public final @NonNull String createdBy;
    public final @NonNull Instant createdAt;

    /**
     * The Persisted Entity
     */
    public final @NonNull T self;

    /**
     * Utility method to give an ID and a creation date to a persistable object
     *
     * @param id        The entity id
     * @param createdAt The creation date time
     * @param entity    The entity object
     * @param <T>       The entity object class
     * @return An identified Object with ID
     */
    public static <T> Entity<T> identify(String id, Instant createdAt, T entity) {
        return new Entity<>(id, NO_ONE, createdAt, entity);
    }

    /**
     * Utility method only for simplify tests. Set createdAt at {@link Instant#EPOCH}
     *
     * @param id     The entity id
     * @param entity The entity object
     * @param <T>    The entity object class
     * @return An identified Object with ID
     */
    public static <T> Entity<T> identify(String id, T entity) {
        return new Entity<>(id, NO_ONE, Instant.EPOCH, entity);
    }

    /**
     * Utility method only for simplify tests. Set createdAt at {@link Instant#EPOCH}
     *
     * @param id        The entity id
     * @param createdBy The {@link fr.ght1pc9kc.baywatch.security.api.model.User} ID who create the Entity
     * @param entity    The entity object
     * @param <T>       The entity object class
     * @return An identified Object with ID
     */
    public static <T> Entity<T> identify(@NotNull String id, @NotNull String createdBy, @NotNull T entity) {
        return new Entity<>(id, createdBy, Instant.EPOCH, entity);
    }
}
