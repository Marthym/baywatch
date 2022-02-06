package fr.ght1pc9kc.baywatch.infra.common.mappers;

import lombok.experimental.UtilityClass;

import javax.annotation.Nullable;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Optional;

@UtilityClass
public class DateUtils {
    /**
     * The forced, specified and mandatory {@link ZoneOffset} to use in NORA project
     */
    public static final ZoneOffset DEFAULT_ZONE_OFFSET = ZoneOffset.UTC;

    /**
     * Convert {@link LocalDateTime} to an {@link Instant} using {@link ZoneOffset#UTC}
     *
     * @param ldt The LocalDateTime
     * @return The matching Instant
     */
    @Nullable
    public static Instant toInstant(@Nullable LocalDateTime ldt) {
        return Optional.ofNullable(ldt)
                .map(t -> t.toInstant(DEFAULT_ZONE_OFFSET))
                .orElse(null);
    }

    /**
     * Convert {@link LocalDate} to an {@link Instant} using {@link ZoneOffset#UTC}
     *
     * @param ld The LocalDate
     * @return The matching Instant
     */
    @Nullable
    public static Instant toInstant(@Nullable LocalDate ld) {
        return Optional.ofNullable(ld)
                .map(t -> t.atStartOfDay().toInstant(DEFAULT_ZONE_OFFSET))
                .orElse(null);
    }

    /**
     * Convert {@link Instant} to an {@link LocalDateTime} using {@link ZoneOffset#UTC}
     *
     * @param t The Instant
     * @return The matching LocalDateTime
     */
    @Nullable
    public static LocalDateTime toLocalDateTime(@Nullable Instant t) {
        return Optional.ofNullable(t)
                .map(i -> i.atOffset(DEFAULT_ZONE_OFFSET).toLocalDateTime())
                .orElse(null);
    }

    /**
     * Convert {@link Instant} to an {@link LocalDate} using {@link ZoneOffset#UTC}
     *
     * @param t The Instant
     * @return The matching LocalDate
     */
    @Nullable
    public static LocalDate toLocalDate(@Nullable Instant t) {
        return Optional.ofNullable(t)
                .map(i -> i.atOffset(DEFAULT_ZONE_OFFSET).toLocalDateTime())
                .map(LocalDateTime::toLocalDate)
                .orElse(null);
    }
}
