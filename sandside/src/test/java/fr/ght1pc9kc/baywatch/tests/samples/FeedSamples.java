package fr.ght1pc9kc.baywatch.tests.samples;

import fr.ght1pc9kc.baywatch.techwatch.api.model.WebFeed;
import fr.ght1pc9kc.entity.api.Entity;

import java.net.URI;
import java.time.Instant;
import java.util.List;
import java.util.Set;

import static fr.ght1pc9kc.baywatch.common.api.model.FeedMeta.updated;

public final class FeedSamples {
    public static final Entity<WebFeed> JEDI = Entity.identify(
                    WebFeed.builder()
                            .name("Jedi Feed")
                            .location(URI.create("https://www.jedi.com/"))
                            .tags(Set.of())
                            .build())
            .meta(updated, Instant.parse("2024-02-25T17:25:42Z"))
            .withId("5fc2a11c3788ce8a200c5c498ed2a8fa3177fe652916ca1e09a85be23077d543");

    public static final Entity<WebFeed> SITH = Entity.identify(
                    WebFeed.builder()
                            .name("Sith Feed")
                            .location(URI.create("https://www.sith.com/"))
                            .tags(Set.of())
                            .build())
            .meta(updated, Instant.parse("2024-02-25T17:25:42Z"))
            .withId("0fdde474b3817af529e3d66ef6c6e8e008dfa6d24d8b02296831bdeb9f0976c3");

    public static final Entity<WebFeed> UNSECURE_PROTOCOL = Entity.identify(
                    WebFeed.builder()
                            .name("Unsecure Protocol")
                            .location(URI.create("files://localhost/.env"))
                            .tags(Set.of())
                            .build())
            .meta(updated, Instant.parse("2024-02-25T17:25:42Z"))
            .withId("e341d3fdf81148da31088a4caf45cb8b9b6e959f41ea6402d199345db3c898d1");

    public static final List<Entity<WebFeed>> SAMPLES = List.of(JEDI, SITH);
}
