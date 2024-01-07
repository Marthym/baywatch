package fr.ght1pc9kc.baywatch.common.infra.mappers;

import fr.ght1pc9kc.baywatch.common.api.model.Entity;
import fr.ght1pc9kc.baywatch.common.domain.DateUtils;
import fr.ght1pc9kc.baywatch.common.domain.Hasher;
import fr.ght1pc9kc.baywatch.techwatch.api.model.WebFeed;
import org.jooq.Field;
import org.jooq.Record;
import org.jooq.impl.DSL;
import org.jooq.impl.DefaultConfiguration;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.net.URI;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import static fr.ght1pc9kc.baywatch.dsl.tables.Feeds.FEEDS;
import static fr.ght1pc9kc.baywatch.dsl.tables.FeedsUsers.FEEDS_USERS;
import static org.assertj.core.api.Assertions.assertThat;

class RecordToFeedConverterTest {
    private static final Instant PUBLICATION = Instant.parse("2020-11-30T20:09:42Z");
    private static final URI TEST_URL = URI.create("https://blog.ght1pc9kc.fr/index.xml");
    private static final String TEST_SHA3 = Hasher.identify(TEST_URL);

    BaywatchMapper tested;

    @BeforeEach
    void setUp() {
        tested = Mappers.getMapper(BaywatchMapper.class);
    }

    @Test
    void should_convert_FeedRecord_to_Feed_pojo() {
        List<Field<?>> fields = new ArrayList<>(Arrays.asList(FEEDS.fields()));
        fields.add(FEEDS_USERS.FEUS_TAGS);

        Record record = DSL.using(new DefaultConfiguration()).newRecord(fields);
        record.set(FEEDS.FEED_ID, TEST_SHA3);
        record.set(FEEDS.FEED_NAME, "Blog ght1pc9kc");
        record.set(FEEDS.FEED_URL, TEST_URL.toString());
        record.set(FEEDS.FEED_LAST_WATCH, DateUtils.toLocalDateTime(PUBLICATION));
        record.set(FEEDS_USERS.FEUS_TAGS, "jedi,light");

        Entity<WebFeed> actual = tested.recordToFeed(record);

        Assertions.assertAll(
                () -> assertThat(actual.id()).isEqualTo(TEST_SHA3),
                () -> assertThat(actual.self().reference()).isEqualTo(TEST_SHA3),
                () -> assertThat(actual.self().name()).isEqualTo("Blog ght1pc9kc"),
                () -> assertThat(actual.self().location()).isEqualTo(TEST_URL),
                () -> assertThat(actual.self().tags()).isEqualTo(Set.of("jedi", "light"))
        );
    }
}