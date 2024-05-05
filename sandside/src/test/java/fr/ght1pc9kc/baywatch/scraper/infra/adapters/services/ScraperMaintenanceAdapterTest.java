package fr.ght1pc9kc.baywatch.scraper.infra.adapters.services;

import fr.ght1pc9kc.baywatch.scraper.api.model.AtomFeed;
import fr.ght1pc9kc.baywatch.techwatch.api.SystemMaintenanceService;
import fr.ght1pc9kc.baywatch.techwatch.api.model.WebFeed;
import fr.ght1pc9kc.baywatch.techwatch.infra.config.TechwatchMapper;
import fr.ght1pc9kc.entity.api.Entity;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

class ScraperMaintenanceAdapterTest {

    private ScraperMaintenanceAdapter tested;
    private SystemMaintenanceService mockMaintenanceService;

    @BeforeEach
    void setUp() {
        mockMaintenanceService = mock(SystemMaintenanceService.class);
        doReturn(Flux.just("42", "66")).when(mockMaintenanceService).newsIdList(any());
        doReturn(Mono.empty().then()).when(mockMaintenanceService).feedsUpdate(anyCollection());
        TechwatchMapper techwatchMapper = Mappers.getMapper(TechwatchMapper.class);
        tested = new ScraperMaintenanceAdapter(mockMaintenanceService, techwatchMapper);
    }

    @Test
    void should_list_all_news_id() {
        StepVerifier.create(tested.listAllNewsId())
                .expectNextCount(2)
                .verifyComplete();
    }

    @Test
    void should_map_feed_list() throws URISyntaxException {
        URI expectedUri = new URI("https://test.local/test");
        doReturn(Flux.just(Entity.identify(WebFeed.builder()
                .name("Test Name")
                .location(expectedUri)
                .tags(Set.of())
                .build()).withId("42"))).when(mockMaintenanceService).feedList(any());

        StepVerifier.create(tested.feedList())
                .assertNext(actual -> SoftAssertions.assertSoftly(softly -> {
                    softly.assertThat(actual.id()).isEqualTo("42");
                    softly.assertThat(actual.link()).isEqualTo(expectedUri);
                })).verifyComplete();
    }

    @Test
    void should_map_feed_updates() throws URISyntaxException {
        URI expectedUri = new URI("https://test.local/test");
        doReturn(Flux.just(Entity.identify(WebFeed.builder()
                .name("Test Name")
                .location(expectedUri)
                .tags(Set.of())
                .build()).withId("42"))).when(mockMaintenanceService).feedList(any());

        StepVerifier.create(tested.feedsUpdate(List.of(
                        Entity.identify(AtomFeed.of("42", new URI("https://test.local/test"))).withId("42"))))
                .verifyComplete();
    }
}