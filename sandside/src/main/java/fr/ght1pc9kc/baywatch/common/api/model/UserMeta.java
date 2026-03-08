package fr.ght1pc9kc.baywatch.common.api.model;

import fr.ght1pc9kc.entity.api.TypedMeta;
import lombok.RequiredArgsConstructor;

import java.time.Instant;
import java.util.Locale;

@RequiredArgsConstructor
@SuppressWarnings("java:S115")
public enum UserMeta implements TypedMeta {
    createdAt(Instant.class),
    createdBy(String.class),
    loginAt(Instant.class),
    /**
     * Current or last IP used to log in
     */
    loginIP(String.class),
    locale(Locale.class),
    userAgent(String.class),
    ;

    private final Class<?> type;

    public final Class<?> type() {
        return type;
    }
}
