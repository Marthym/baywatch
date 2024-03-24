package fr.ght1pc9kc.baywatch.indexer.infra.adapters;

import fr.ght1pc9kc.baywatch.indexer.domain.model.IndexableFeed;
import fr.ght1pc9kc.baywatch.techwatch.api.model.WebFeed;
import fr.ght1pc9kc.entity.api.Entity;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.net.URI;
import java.util.Set;

class IndexerMapperTest {
    private final IndexerMapper tested = Mappers.getMapper(IndexerMapper.class);

    @Test
    void should_map_webfeed_to_indexable_feed() {
        IndexableFeed actual = tested.getIndexableFromFeed(Entity.identify(WebFeed.builder()
                .name("Obiwan")
                .description("Kenobi")
                .location(URI.create("https;//www.jedi.com/"))
                .tags(Set.of("jedi", "sith"))
                .build()).withId("42"));
        SoftAssertions.assertSoftly(soft -> {
            soft.assertThat(actual.id()).isEqualTo("42");
            soft.assertThat(actual.title()).isEqualTo("Obiwan");
            soft.assertThat(actual.description()).isEqualTo("Kenobi");
            soft.assertThat(actual.link()).isEqualTo("https;//www.jedi.com/");
            soft.assertThat(actual.tags()).containsOnly("jedi", "sith");
        });
    }
}