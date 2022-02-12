package fr.ght1pc9kc.baywatch.infra.mappers;

import fr.ght1pc9kc.baywatch.domain.utils.DateUtils;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

import static org.assertj.core.api.Assertions.assertThat;

class DateUtilsTest {
    private static final Instant EXPECTED_INSTANT = Instant.parse("2019-04-02T09:28:56.262Z");


    @Test
    void should_convert_LocalDateTime_to_instant() {
        assertThat(DateUtils.toInstant((LocalDateTime) null)).isNull();
        Instant actual = DateUtils.toInstant(LocalDateTime.parse("2019-04-02T09:28:56.262"));
        assertThat(actual).isEqualTo(EXPECTED_INSTANT);
    }

    @Test
    void should_convert_LocalDate_to_instant() {
        assertThat(DateUtils.toInstant((LocalDate) null)).isNull();
        Instant actual = DateUtils.toInstant(LocalDate.parse("2019-04-02"));
        assertThat(actual).isEqualTo(EXPECTED_INSTANT.truncatedTo(ChronoUnit.DAYS));
    }

    @Test
    void should_convert_instant_to_localdatetime() {
        assertThat(DateUtils.toLocalDateTime(null)).isNull();
        LocalDateTime actual = DateUtils.toLocalDateTime(EXPECTED_INSTANT);
        assertThat(actual).isEqualTo(LocalDateTime.parse("2019-04-02T09:28:56.262"));
    }

    @Test
    void should_convert_instant_to_localdate() {
        assertThat(DateUtils.toLocalDate(null)).isNull();
        LocalDate actual = DateUtils.toLocalDate(EXPECTED_INSTANT);
        assertThat(actual).isEqualTo(LocalDate.parse("2019-04-02"));
    }
}