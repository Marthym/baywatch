package fr.ght1pc9kc.baywatch.scraper.infra.config;

import fr.ght1pc9kc.baywatch.scraper.api.model.AtomEntry;
import fr.ght1pc9kc.baywatch.techwatch.api.model.News;
import fr.ght1pc9kc.baywatch.techwatch.api.model.RawNews;
import fr.ght1pc9kc.baywatch.techwatch.api.model.State;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.net.URI;
import java.time.Instant;
import java.util.Set;

class ScraperMapperTest {
    ScraperMapper tested = Mappers.getMapper(ScraperMapper.class);

    @Test
    void should_get_news_from_atom() {
        Assertions.assertThat(tested.getAtomFromNews(News.builder().raw(RawNews.builder()
                                .id("42")
                                .title("May the force")
                                .description("May the Force be with you, Always")
                                .image(URI.create("http://jedi.com/favicon.ico"))
                                .publication(Instant.parse("2022-07-01T10:10:10Z"))
                                .link(URI.create("http://jedi.com/may_the_force.html"))
                                .build())
                        .feeds(Set.of("FEED42", "FEED24"))
                        .state(State.NONE)
                        .build()))
                .isEqualTo(new AtomEntry(
                        "42", "May the force",
                        URI.create("http://jedi.com/favicon.ico"),
                        "May the Force be with you, Always", Instant.parse("2022-07-01T10:10:10Z"),
                        URI.create("http://jedi.com/may_the_force.html"),
                        Set.of("FEED42", "FEED24")));
    }
}