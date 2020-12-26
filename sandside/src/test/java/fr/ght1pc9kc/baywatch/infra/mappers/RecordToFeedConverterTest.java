package fr.ght1pc9kc.baywatch.infra.mappers;

import fr.ght1pc9kc.baywatch.api.model.Feed;
import fr.ght1pc9kc.baywatch.api.model.RawFeed;
import fr.ght1pc9kc.baywatch.domain.utils.Hasher;
import org.jooq.Field;
import org.jooq.Record;
import org.jooq.impl.DSL;
import org.jooq.impl.DefaultConfiguration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.convert.support.DefaultConversionService;

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
    private static final String TEST_SHA3 = Hasher.sha3(TEST_URL.toString());

    RecordToFeedConverter tested;

    @BeforeEach
    void setUp() {
        DefaultConversionService conversionService = new DefaultConversionService();
        conversionService.addConverter(new RecordToRawFeedConverter());
        tested = new RecordToFeedConverter();
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

        Feed actual = tested.convert(record);

        assertThat(actual).isEqualTo(Feed.builder().raw(RawFeed.builder()
                .id(TEST_SHA3)
                .name("Blog ght1pc9kc")
                .lastWatch(PUBLICATION)
                .url(TEST_URL)
                .build())
                .tags(Set.of("jedi", "light")).build());
    }
}