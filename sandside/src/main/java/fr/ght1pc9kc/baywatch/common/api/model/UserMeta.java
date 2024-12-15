package fr.ght1pc9kc.baywatch.common.api.model;

import fr.ght1pc9kc.entity.api.TypedMeta;
import lombok.RequiredArgsConstructor;

import java.time.Instant;

@RequiredArgsConstructor
@SuppressWarnings("java:S115")
public enum UserMeta implements TypedMeta {
    createdAt(Instant.class),
    createdBy(String.class),
    loginAt(Instant.class),
    loginIP(String.class);

    private final Class<?> type;

    public final Class<?> type() {
        return type;
    }
}
