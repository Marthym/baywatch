package fr.ght1pc9kc.baywatch.common.api;

import fr.ght1pc9kc.entity.api.TypedMeta;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;

import java.time.Instant;

@Getter
@RequiredArgsConstructor
@Accessors(fluent = true)
@SuppressWarnings("java:S115")
public enum DefaultMeta implements TypedMeta {
    createdAt(Instant.class), createdBy(String.class);
    public static final String NO_ONE = "_";

    private final Class<?> type;
}
