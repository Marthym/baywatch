package fr.ght1pc9kc.baywatch.api.common.model;

import lombok.EqualsAndHashCode;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.time.Instant;

/**
 * Hold the standard Persistence information
 *
 * @param <T> The type of the persisted object
 */
@ToString
@EqualsAndHashCode
@RequiredArgsConstructor
public final class Entity<T> {
    public static final String IDENTIFIER = "_id";
    public static final String CREATED_AT = "_createdAt";

    @NonNull
    public final String id;
    @NonNull
    public final Instant createdAt;

    /**
     * The Persisted Entity
     */
    @NonNull
    public final T entity;

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
        return new Entity<>(id, createdAt, entity);
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
        return new Entity<>(id, Instant.EPOCH, entity);
    }
}
