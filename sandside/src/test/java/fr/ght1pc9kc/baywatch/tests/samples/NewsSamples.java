package fr.ght1pc9kc.baywatch.tests.samples;

import fr.ght1pc9kc.baywatch.common.domain.Hasher;
import fr.ght1pc9kc.baywatch.techwatch.api.model.Flags;
import fr.ght1pc9kc.baywatch.techwatch.api.model.News;
import fr.ght1pc9kc.baywatch.techwatch.api.model.RawNews;
import fr.ght1pc9kc.baywatch.techwatch.api.model.State;

import java.net.URI;
import java.time.Instant;
import java.util.List;
import java.util.Set;

public final class NewsSamples {
    public static final News MAY_THE_FORCE = News.builder()
            .raw(RawNews.builder()
                    .id("3fbe6f22297571d2a4b1f35c8c08fe3b2aaa17c155b4c3b2fc842b3d188f55e9")
                    .link(URI.create("https://www.force.com/may-the-force"))
                    .title("May the Force be with you")
                    .description("May the Force be with you")
                    .publication(Instant.parse("2021-04-13T22:00:42Z"))
                    .build())
            .state(State.of(Flags.ALL))
            .build();

    public static final News ORDER_66 = News.builder()
            .raw(RawNews.builder()
                    .id("bd32550e3963aed4aa6fead627ddc694e31a91d0e7b85cfa68e1c5fd7a4a9277")
                    .link(URI.create("https://www.empire.com/order-66"))
                    .title("Give the order")
                    .description("Give the order")
                    .publication(Instant.parse("2021-04-12T22:00:42Z"))
                    .build())
            .state(State.of(Flags.NONE))
            .build();

    public static final News A_NEW_HOPE = News.builder()
            .raw(RawNews.builder()
                    .id(Hasher.sha3("A_NEW_HOPE"))
                    .title("A New Hope")
                    .link(URI.create("https://www.force.com/a_new_hope"))
                    .description("A New Hope")
                    .publication(Instant.parse("2022-04-01T22:00:42Z"))
                    .build())
            .state(State.of(Flags.READ))
            .feeds(Set.of(FeedSamples.JEDI.id()))
            .build();

    public static final List<News> SAMPLES = List.of(MAY_THE_FORCE, ORDER_66);
}
