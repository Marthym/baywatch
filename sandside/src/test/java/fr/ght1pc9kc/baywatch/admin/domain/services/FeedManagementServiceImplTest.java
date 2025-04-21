package fr.ght1pc9kc.baywatch.admin.domain.services;

import fr.ght1pc9kc.baywatch.admin.api.model.RawFeed;
import fr.ght1pc9kc.baywatch.admin.domain.ports.RawFeedPersistencePort;
import fr.ght1pc9kc.baywatch.admin.domain.ports.RawNewsPersistencePort;
import fr.ght1pc9kc.baywatch.common.domain.Hasher;
import fr.ght1pc9kc.baywatch.common.domain.QueryContext;
import fr.ght1pc9kc.entity.api.Entity;
import fr.ght1pc9kc.juery.api.PageRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.net.URI;
import java.util.Collection;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class FeedManagementServiceImplTest {

    private FeedManagementServiceImpl tested;

    private final RawFeedPersistencePort feedPersistencePort = mock(RawFeedPersistencePort.class);
    private final RawNewsPersistencePort newsPersistencePort = mock(RawNewsPersistencePort.class);

    @BeforeEach
    void setUp() {
        doReturn(Flux.just(Entity.identify(RawFeed.builder().build()).withId("test-id")))
                .when(feedPersistencePort).list(any(QueryContext.class));
        doReturn(Mono.just(1L)).when(feedPersistencePort).count(any(QueryContext.class));
        doReturn(Flux.just(Entity.identify(RawFeed.builder().build()).withId("test-id")))
                .when(feedPersistencePort).persist(any());
        doReturn(Flux.just(Entity.identify(RawFeed.builder().build()).withId("test-id")))
                .when(feedPersistencePort).update(any());
        doReturn(Mono.empty()).when(newsPersistencePort).delete(any(QueryContext.class));
        doReturn(Mono.empty()).when(feedPersistencePort).delete(any(QueryContext.class));

        tested = new FeedManagementServiceImpl(feedPersistencePort, newsPersistencePort);
    }

    @Test
    void should_get_raw_feed() {
        StepVerifier.create(tested.get("test-id"))
                .expectNextCount(1)
                .verifyComplete();
        verify(feedPersistencePort).list(any());
    }

    @Test
    void should_find_raw_feeds() {
        StepVerifier.create(tested.find(PageRequest.all()))
                .expectNextCount(1)
                .verifyComplete();
        verify(feedPersistencePort).list(any());
    }

    @Test
    void should_count_raw_feeds() {
        StepVerifier.create(tested.count(PageRequest.all()))
                .expectNextCount(1)
                .verifyComplete();
        verify(feedPersistencePort).count(any());
    }

    @Test
    @SuppressWarnings("unchecked")
    void should_create_raw_feed() {
        StepVerifier.create(tested.create(RawFeed.builder()
                        .url(URI.create("https://test.test/"))
                        .build()))
                .expectNextCount(1)
                .verifyComplete();
        verify(feedPersistencePort).persist((Collection<Entity<RawFeed>>) any(Collection.class));
    }

    @Test
    @SuppressWarnings("unchecked")
    void should_update_raw_feed() {
        StepVerifier.create(tested.update(Hasher.identify(URI.create("https://test.test/")), RawFeed.builder()
                        .url(URI.create("https://test.test/"))
                        .build()))
                .expectNextCount(1)
                .verifyComplete();
        verify(feedPersistencePort).update((Collection<Entity<RawFeed>>) any(Collection.class));
    }

    @Test
    void should_delete_raw_feeds() {
        StepVerifier.create(tested.delete(List.of("42"))).verifyComplete();
        verify(newsPersistencePort).delete(any(QueryContext.class));
        verify(feedPersistencePort).delete(any(QueryContext.class));
    }
}