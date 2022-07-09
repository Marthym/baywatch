package fr.ght1pc9kc.baywatch.techwatch.infra.config;

import fr.ght1pc9kc.baywatch.scraper.api.model.AtomFeed;
import fr.ght1pc9kc.baywatch.techwatch.api.model.Feed;
import fr.ght1pc9kc.baywatch.techwatch.api.model.RawFeed;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.net.URI;
import java.util.Set;

class TechwatchMapperTest {
    private final TechwatchMapper tested = Mappers.getMapper(TechwatchMapper.class);

    @Test
    void should_map_AtomFeed_to_Feed() {
        Assertions.assertThat(tested.getFeedFromAtom(new AtomFeed(null,
                "Jedi Channel", "May the force be with you",
                "Obiwan Kenobi", URI.create("https://jedi.com/feed/")))
        ).isEqualTo(Feed.builder()
                .name("Jedi Channel")
                .tags(Set.of())
                .raw(RawFeed.builder()
                        .id("1d55e5018b8c189cd73be7d3177410edfb98831b172153f1a985b47f3d666ffd")
                        .name("Jedi Channel")
                        .description("May the force be with you")
                        .url(URI.create("https://jedi.com/feed/"))
                        .build())
                .build());
    }
}