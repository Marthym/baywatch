package fr.ght1pc9kc.baywatch.common.api.model;

import fr.ght1pc9kc.entity.api.TypedMeta;
import lombok.RequiredArgsConstructor;

import java.time.Instant;

@RequiredArgsConstructor
@SuppressWarnings("java:S115")
public enum FeedMeta implements TypedMeta {
    ETag(String.class),
    createdBy(String.class),
    updated(Instant.class);

    private final Class<?> type;

    public final Class<?> type() {
        return type;
    }
}
