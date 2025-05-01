package fr.ght1pc9kc.baywatch.admin.infra.mappers;

import fr.ght1pc9kc.baywatch.admin.api.model.RawFeed;
import fr.ght1pc9kc.baywatch.common.api.model.FeedMeta;
import fr.ght1pc9kc.baywatch.techwatch.api.model.WebFeed;
import fr.ght1pc9kc.entity.api.Entity;
import org.assertj.core.api.Assertions;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.net.URI;
import java.time.Instant;
import java.util.Map;

import static java.util.Objects.requireNonNull;

class RawFeedMapperTest {
    private final RawFeedMapper tested = Mappers.getMapper(RawFeedMapper.class);

    @Test
    void should_convert_webfeed_to_rawfeed() {
        Entity<WebFeed> webFeed = Entity.identify(WebFeed.builder()
                        .name("Jedi Archives News")
                        .location(URI.create("https://holonet.jediarchives.coruscant/feed"))
                        .icon(URI.create("https://holonet.jediarchives.coruscant/icon.png"))
                        .description("Latest updates from the Jedi Temple Archives")
                        .tag("jedi")
                        .build())
                .meta(FeedMeta.ETag, "force-etag")
                .meta(FeedMeta.updated, Instant.parse("2023-01-01T00:00:00Z"))
                .withId("jedi-archives-001");

        Entity<RawFeed> actual = tested.toRawFeed(webFeed);

        Assertions.assertThat(actual)
                .isNotNull()
                .satisfies(r -> SoftAssertions.assertSoftly(soft -> {
                    soft.assertThat(r.id()).isEqualTo("jedi-archives-001");
                    soft.assertThat(r.self().name()).isEqualTo("Jedi Archives News");
                    soft.assertThat(r.self().url().toString()).isEqualTo("https://holonet.jediarchives.coruscant/feed");
                    soft.assertThat(r.self().icon().toString()).isEqualTo("https://holonet.jediarchives.coruscant/icon.png");
                    soft.assertThat(r.self().description()).isEqualTo("Latest updates from the Jedi Temple Archives");
                    soft.assertThat(r.self().lastETag()).isEqualTo("force-etag");
                    soft.assertThat(r.self().lastWatch()).isEqualTo(Instant.parse("2023-01-01T00:00:00Z"));
                }));
    }

    @Test
    void should_convert_rawfeed_to_webfeed() {
        Entity<RawFeed> rawFeed = Entity.identify(RawFeed.builder()
                        .name("Imperial HoloNet News")
                        .url(URI.create("https://holonet.empire.gov/news/feed"))
                        .icon(URI.create("https://holonet.empire.gov/news/feed.png"))
                        .description("Official news from the Galactic Empire")
                        .lastETag("imperial-etag")
                        .lastWatch(Instant.parse("2023-12-25T12:00:00Z"))
                        .build())
                .withId("empire-news-001");

        Entity<WebFeed> actual = tested.toWebFeed(rawFeed);

        Assertions.assertThat(actual)
                .isNotNull()
                .satisfies(w -> SoftAssertions.assertSoftly(soft -> {
                    soft.assertThat(w.id()).isEqualTo("empire-news-001");
                    soft.assertThat(w.self().name()).isEqualTo("Imperial HoloNet News");
                    soft.assertThat(w.self().location().toString()).isEqualTo("https://holonet.empire.gov/news/feed");
                    soft.assertThat(requireNonNull(w.self().icon()).toString())
                            .isEqualTo("https://holonet.empire.gov/news/feed.png");
                    soft.assertThat(w.self().description()).isEqualTo("Official news from the Galactic Empire");
                    soft.assertThat(w.meta(FeedMeta.ETag)).contains("imperial-etag");
                    soft.assertThat(w.meta(FeedMeta.updated, Instant.class))
                            .contains(Instant.parse("2023-12-25T12:00:00Z"));
                }));
    }

    @Test
    void should_convert_rawfeed_entity_to_map_object() {

        Entity<RawFeed> rawFeed = Entity.identify(RawFeed.builder()
                        .name("Rebel Alliance News Network")
                        .url(URI.create("https://alliance.rebels.org/feed"))
                        .icon(URI.create("https://alliance.rebels.org/feed.png"))
                        .description("Secret transmissions from the Rebel Alliance")
                        .lastETag("rebel-base-etag")
                        .lastWatch(Instant.parse("2023-05-04T12:00:00Z"))
                        .build())
                .withId("rebel-news-001");

        Map<String, Object> actual = tested.convertValue(rawFeed);


        SoftAssertions.assertSoftly(soft -> {
            soft.assertThat(actual).isNotNull();
            soft.assertThat(actual).containsEntry("_id", "rebel-news-001");
            soft.assertThat(actual).containsEntry("name", "Rebel Alliance News Network");
            soft.assertThat(actual).containsEntry("url", URI.create("https://alliance.rebels.org/feed"));
            soft.assertThat(actual).containsEntry("icon", URI.create("https://alliance.rebels.org/feed.png"));
            soft.assertThat(actual).containsEntry("description", "Secret transmissions from the Rebel Alliance");
            soft.assertThat(actual).containsEntry("lastETag", "rebel-base-etag");
            soft.assertThat(actual).containsEntry("lastWatch", Instant.parse("2023-05-04T12:00:00Z"));
        });
    }
}