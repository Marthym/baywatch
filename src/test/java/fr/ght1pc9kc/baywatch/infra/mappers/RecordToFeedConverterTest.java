package fr.ght1pc9kc.baywatch.infra.mappers;

import fr.ght1pc9kc.baywatch.api.model.Feed;
import fr.ght1pc9kc.baywatch.domain.utils.Hasher;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.time.Instant;

import static fr.ght1pc9kc.baywatch.dsl.tables.Feeds.FEEDS;
import static org.assertj.core.api.Assertions.assertThat;

class RecordToFeedConverterTest {
    private static final Instant PUBLICATION = Instant.parse("2020-11-30T20:09:42Z");
    private static final URI TEST_URL = URI.create("https://blog.ght1pc9kc.fr/index.xml");
    private static final String TEST_SHA3 = Hasher.sha3(TEST_URL.toString());

    RecordToFeedConverter tested = new RecordToFeedConverter();

    @Test
    void should_convert_FeedRecord_to_Feed_pojo() {
        Feed actual = tested.convert(FEEDS.newRecord()
                .setFeedId(TEST_SHA3)
                .setFeedName("Blog ght1pc9kc")
                .setFeedUrl(TEST_URL.toString())
                .setFeedLastWatch(DateUtils.toLocalDateTime(PUBLICATION))
        );

        assertThat(actual).isEqualTo(Feed.builder()
                .id(TEST_SHA3)
                .name("Blog ght1pc9kc")
                .lastWatch(PUBLICATION)
                .url(TEST_URL)
                .build());
    }
}