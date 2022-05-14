package fr.ght1pc9kc.baywatch.scraper.domain.actions;

import fr.ght1pc9kc.baywatch.techwatch.api.NewsService;
import fr.ght1pc9kc.baywatch.techwatch.api.SystemMaintenanceService;
import fr.ght1pc9kc.baywatch.techwatch.api.model.Feed;
import fr.ght1pc9kc.baywatch.techwatch.api.model.News;
import fr.ght1pc9kc.baywatch.tests.samples.FeedSamples;
import fr.ght1pc9kc.baywatch.tests.samples.NewsSamples;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.Collection;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class DeleteOrphanFeedHandlerTest {

    private DeleteOrphanFeedHandler tested;
    private SystemMaintenanceService systemMaintenanceMock = mock(SystemMaintenanceService.class);

    @BeforeEach
    void setUp() {
        when(systemMaintenanceMock.feedList(any())).thenReturn(Flux.fromIterable(FeedSamples.SAMPLES).map(Feed::getRaw));
        when(systemMaintenanceMock.feedDelete(anyCollection())).thenReturn(Mono.just(2));
        when(systemMaintenanceMock.newsList(any())).thenReturn(Flux.fromIterable(NewsSamples.SAMPLES).map(News::getRaw));
        when(systemMaintenanceMock.newsDelete(anyCollection())).thenReturn(Mono.just(2));

        NewsService newsServiceMock = mock(NewsService.class);
        when(newsServiceMock.list(any())).thenReturn(Flux.fromIterable(NewsSamples.SAMPLES));
        when(newsServiceMock.delete(anyCollection())).thenReturn(Mono.just(1));

        tested = new DeleteOrphanFeedHandler(systemMaintenanceMock);
    }

    @Test
    @SuppressWarnings("unchecked")
    void should_delete_orphans_feed() {
        StepVerifier.create(tested.before())
                .verifyComplete();

        ArgumentCaptor<Collection<String>> captor = ArgumentCaptor.forClass(Collection.class);
        verify(systemMaintenanceMock).feedDelete(captor.capture());
        Assertions.assertThat(captor.getValue()).containsExactly(
                "5fc2a11c3788ce8a200c5c498ed2a8fa3177fe652916ca1e09a85be23077d543",
                "0fdde474b3817af529e3d66ef6c6e8e008dfa6d24d8b02296831bdeb9f0976c3");

        verify(systemMaintenanceMock).newsDelete(captor.capture());
        Assertions.assertThat(captor.getValue()).containsExactly(
                "3fbe6f22297571d2a4b1f35c8c08fe3b2aaa17c155b4c3b2fc842b3d188f55e9",
                "bd32550e3963aed4aa6fead627ddc694e31a91d0e7b85cfa68e1c5fd7a4a9277",
                "3fbe6f22297571d2a4b1f35c8c08fe3b2aaa17c155b4c3b2fc842b3d188f55e9",
                "bd32550e3963aed4aa6fead627ddc694e31a91d0e7b85cfa68e1c5fd7a4a9277");
    }
}