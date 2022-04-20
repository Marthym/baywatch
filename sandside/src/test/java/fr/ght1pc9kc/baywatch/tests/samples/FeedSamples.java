package fr.ght1pc9kc.baywatch.tests.samples;

import fr.ght1pc9kc.baywatch.techwatch.api.model.Feed;
import fr.ght1pc9kc.baywatch.techwatch.api.model.RawFeed;

import java.net.URI;
import java.util.List;

public final class FeedSamples {
    public static final Feed JEDI = Feed.builder()
            .raw(RawFeed.builder()
                    .id("9e9195bd69117a6091e8865185ac8c04d099cbc92d7a465990cc32392e1c1f06")
                    .name("Jedi Feed")
                    .url(URI.create("https://www.jedi.com/"))
                    .build())
            .build();

    public static final Feed SITH = Feed.builder()
            .raw(RawFeed.builder()
                    .id("29950200eacfb32e79855986ae5cc41fb9fdbdd80629e856fd654f0c64769329")
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
