package fr.ght1pc9kc.baywatch.techwatch.infra.config;

import fr.ght1pc9kc.baywatch.common.domain.DateUtils;
import fr.ght1pc9kc.baywatch.scraper.api.model.AtomFeed;
import fr.ght1pc9kc.baywatch.techwatch.api.model.WebFeed;
import fr.ght1pc9kc.baywatch.tests.samples.FeedSamples;
import fr.ght1pc9kc.entity.api.Entity;
import org.assertj.core.api.Assertions;
import org.jooq.Field;
import org.jooq.Record;
import org.jooq.impl.DSL;
import org.jooq.impl.DefaultConfiguration;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.net.URI;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import static fr.ght1pc9kc.baywatch.dsl.tables.Feeds.FEEDS;
import static org.assertj.core.api.Assertions.assertThat;

class TechwatchMapperTest {
    private final TechwatchMapper tested = Mappers.getMapper(TechwatchMapper.class);

    @Test
    void should_convert_FeedRecord_to_Feed_pojo() {
        List<Field<?>> fields = new ArrayList<>(Arrays.asList(FEEDS.fields()));

        Record feedRecord = DSL.using(new DefaultConfiguration()).newRecord(fields);
        feedRecord.set(FEEDS.FEED_ID, FeedSamples.JEDI.id());
        feedRecord.set(FEEDS.FEED_NAME, "Blog ght1pc9kc");
        feedRecord.set(FEEDS.FEED_URL, FeedSamples.JEDI.self().location().toString());
        feedRecord.set(FEEDS.FEED_LAST_WATCH, DateUtils.toLocalDateTime(Instant.parse("2024-09-11T19:05:42Z")));

        Entity<WebFeed> actual = tested.recordToFeed(feedRecord);

        org.junit.jupiter.api.Assertions.assertAll(
                () -> assertThat(actual.id()).isEqualTo(FeedSamples.JEDI.id()),
                () -> assertThat(actual.self().name()).isEqualTo("Blog ght1pc9kc"),
                () -> assertThat(actual.self().location()).isEqualTo(FeedSamples.JEDI.self().location()),
                () -> assertThat(actual.self().tags()).isEmpty()
        );
    }

    @Test
    void should_map_AtomFeed_to_Feed() {
        Assertions.assertThat(tested.getFeedFromAtom(new AtomFeed(null,
                "Jedi Channel", "May the force be with you",
                "Obiwan Kenobi", URI.create("https://jedi.com/feed/"), Instant.parse("2024-02-25T17:11:42Z")))
        ).isEqualTo(WebFeed.builder()
                .name("Jedi Channel")
                .tags(Set.of())
                .name("Jedi Channel")
                .description("May the force be with you")
                .location(URI.create("https://jedi.com/feed/"))
                .build());
    }
}