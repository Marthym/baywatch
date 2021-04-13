package fr.ght1pc9kc.baywatch.domain.samples;

import fr.ght1pc9kc.baywatch.api.model.Flags;
import fr.ght1pc9kc.baywatch.api.model.News;
import fr.ght1pc9kc.baywatch.api.model.RawNews;
import fr.ght1pc9kc.baywatch.api.model.State;

import java.net.URI;
import java.time.Instant;
import java.util.List;

public final class NewsSamples {
    public static final News MAY_THE_FORCE = News.builder()
            .raw(RawNews.builder()
                    .id("3fbe6f22297571d2a4b1f35c8c08fe3b2aaa17c155b4c3b2fc842b3d188f55e9")
                    .link(URI.create("https://www.force.com/may-the-force"))
                    .description("May the Force be with you")
                    .publication(Instant.parse("2021-04-13T22:00:42Z"))
                    .build())
            .state(State.of(Flags.ALL))
            .build();

    public static final News ORDER_66 = News.builder()
            .raw(RawNews.builder()
                    .id("bd32550e3963aed4aa6fead627ddc694e31a91d0e7b85cfa68e1c5fd7a4a9277")
                    .link(URI.create("https://www.empire.com/order-66"))
                    .description("Give the order")
                    .publication(Instant.parse("2021-04-12T22:00:42Z"))
                    .build())
            .state(State.of(Flags.NONE))
            .build();

    public static final List<News> SAMPLES = List.of(MAY_THE_FORCE, ORDER_66);
}
