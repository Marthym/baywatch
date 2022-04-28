package fr.ght1pc9kc.baywatch.tests.samples;

import fr.ght1pc9kc.baywatch.techwatch.api.model.Feed;
import fr.ght1pc9kc.baywatch.techwatch.api.model.RawFeed;

import java.net.URI;
import java.util.List;

public final class FeedSamples {
    public static final Feed JEDI = Feed.builder()
            .raw(RawFeed.builder()
                    .id("5fc2a11c3788ce8a200c5c498ed2a8fa3177fe652916ca1e09a85be23077d543")
                    .name("Jedi Feed")
                    .url(URI.create("https://www.jedi.com/"))
                    .build())
            .build();

    public static final Feed SITH = Feed.builder()
            .raw(RawFeed.builder()
                    .id("0fdde474b3817af529e3d66ef6c6e8e008dfa6d24d8b02296831bdeb9f0976c3")
                    .name("Sith Feed")
                    .url(URI.create("https://www.sith.com/"))
                    .build())
            .build();

    public static final Feed UNSECURE_PROTOCOL = Feed.builder()
            .raw(RawFeed.builder()
                    .id("e341d3fdf81148da31088a4caf45cb8b9b6e959f41ea6402d199345db3c898d1")
                    .name("Unsecure Protocol")
                    .url(URI.create("files://localhost/.env"))
                    .build())
            .build();

    public static final List<Feed> SAMPLES = List.of(JEDI, SITH);
}
