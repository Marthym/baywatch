package fr.ght1pc9kc.baywatch.admin.infra.controllers;

import fr.ght1pc9kc.baywatch.admin.api.FeedManagementService;
import fr.ght1pc9kc.baywatch.admin.api.model.RawFeed;
import fr.ght1pc9kc.baywatch.admin.infra.mappers.RawFeedMapper;
import fr.ght1pc9kc.baywatch.admin.infra.model.AdminRawFeedForm;
import fr.ght1pc9kc.baywatch.admin.infra.model.AdminRawFeedRequest;
import fr.ght1pc9kc.baywatch.common.infra.model.Page;
import fr.ght1pc9kc.entity.api.Entity;
import graphql.GraphQLError;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.springframework.graphql.execution.ErrorType;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.net.URI;
import java.util.Collection;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class FeedManagementControllerTest {
    private FeedManagementController controller;
    private FeedManagementService mockFeedManagementService;
    private RawFeedMapper mockMapper;

    @BeforeEach
    void setUp() {
        mockFeedManagementService = mock(FeedManagementService.class);
        mockMapper = mock(RawFeedMapper.class);
        controller = new FeedManagementController(mockFeedManagementService, mockMapper);
    }

    @Test
    void should_find_raw_feeds() {
        // Given
        var mockedEntity = Entity.identify(RawFeed.builder().name("Test Feed").build()).withId("test-id");
        when(mockFeedManagementService.find(any())).thenReturn(Flux.just(mockedEntity));
        when(mockFeedManagementService.count(any())).thenReturn(Mono.just(1));

        // When
        Mono<Page<Entity<RawFeed>>> result = controller.adminRawFeedFind(new AdminRawFeedRequest(
                1, 10, null, null, null, null, "Test Feed", "https://test.com/feed"
        ));

        // Then
        StepVerifier.create(result)
                .assertNext(page -> SoftAssertions.assertSoftly(soft -> {
                    soft.assertThat(page.getBody()).isNotNull();
                    soft.assertThat(page.getHeaders().toSingleValueMap()).containsEntry("X-Total-Count", "1");
                }))
                .verifyComplete();

        verify(mockFeedManagementService).find(any());
        verify(mockFeedManagementService).count(any());
    }

    @Test
    void should_get_raw_feed_by_id() {
        // Given
        var mockedEntity = Entity.identify(RawFeed.builder().name("Test Feed").build()).withId("test-id");
        when(mockFeedManagementService.get("test-id")).thenReturn(Mono.just(mockedEntity));

        // When
        Mono<Entity<RawFeed>> result = controller.adminRawFeedGet("test-id");

        // Then
        StepVerifier.create(result)
                .assertNext(entity -> assertThat(entity).isNotNull()
                        .satisfies(e -> assertThat(e.self().name()).isEqualTo("Test Feed")))
                .verifyComplete();

        verify(mockFeedManagementService).get("test-id");
    }

    @Test
    void should_update_raw_feed() {
        // Given
        var mockedEntity = Entity.identify(RawFeed.builder().name("Updated Feed").build()).withId("test-id");
        AdminRawFeedForm form = new AdminRawFeedForm(
                "Imperial HoloNet News",
                "Latest updates from the Galactic Empire",
                URI.create("https://holonet.empire.gov/feed"),
                URI.create("https://holonet.empire.gov/icon.png"),
                "2024-01-15T10:30:00Z",
                "death-star-v2-etag"
        );
        when(mockMapper.toRawFeed(form)).thenReturn(mockedEntity.self());
        when(mockFeedManagementService.update(eq("test-id"), any())).thenReturn(Mono.just(mockedEntity));

        // When
        Mono<Entity<RawFeed>> result = controller.adminRawFeedUpdate("test-id", form);

        // Then
        StepVerifier.create(result)
                .consumeNextWith(entity -> assertThat(entity).isNotNull()
                        .satisfies(e -> assertThat(e.self().name()).isEqualTo("Updated Feed")))
                .verifyComplete();

        ArgumentCaptor<RawFeed> captor = ArgumentCaptor.forClass(RawFeed.class);
        verify(mockFeedManagementService).update(eq("test-id"), captor.capture());
        verify(mockMapper).toRawFeed(form);
    }

    @Test
    void should_create_raw_feed() {
        // Given
        var mockedEntity = Entity.identify(RawFeed.builder().name("Created Feed").build()).withId("new-id");
        AdminRawFeedForm form = new AdminRawFeedForm(
                "Imperial HoloNet News",
                "Latest updates from the Galactic Empire",
                URI.create("https://holonet.empire.gov/feed"),
                URI.create("https://holonet.empire.gov/icon.png"),
                "2024-01-15T10:30:00Z",
                "death-star-v2-etag"
        );
        when(mockMapper.toRawFeed(form)).thenReturn(mockedEntity.self());
        when(mockFeedManagementService.create(any())).thenReturn(Mono.just(mockedEntity));

        // When
        Mono<Entity<RawFeed>> result = controller.adminRawFeedCreate(form);

        // Then
        StepVerifier.create(result)
                .assertNext(entity -> assertThat(entity).isNotNull()
                        .satisfies(e -> assertThat(e.self().name()).isEqualTo("Created Feed")))
                .verifyComplete();

        verify(mockFeedManagementService).create(any());
        verify(mockMapper).toRawFeed(form);
    }

    @Test
    @SuppressWarnings("unchecked")
    void should_delete_raw_feeds() {
        // Given
        when(mockFeedManagementService.delete(any())).thenReturn(Mono.empty());

        // When
        Mono<Void> result = controller.adminRawFeedDelete(List.of("id1", "id2"));

        // Then
        StepVerifier.create(result).verifyComplete();

        ArgumentCaptor<Collection<String>> captor = ArgumentCaptor.forClass(Collection.class);
        verify(mockFeedManagementService).delete(captor.capture());
        assertThat(captor.getValue()).containsExactly("id1", "id2");
    }

    @ParameterizedTest
    @MethodSource("exceptionProvider")
    void should_handle_different_exceptions_and_return_correct_graphql_error(
            Exception exception, ErrorType expectedType, String expectedMessage) {
        // When
        GraphQLError result = controller.handle(exception);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getErrorType()).isEqualTo(expectedType);
        assertThat(result.getMessage()).isEqualTo(expectedMessage);
    }

    private static Stream<Arguments> exceptionProvider() {
        return Stream.of(
                Arguments.of(new NullPointerException("This is a null pointer exception"),
                        ErrorType.BAD_REQUEST, "This is a null pointer exception"),
                Arguments.of(new NoSuchElementException("This is a no such element exception"),
                        ErrorType.NOT_FOUND, "This is a no such element exception"),
                Arguments.of(new RuntimeException("This is a runtime exception"),
                        ErrorType.INTERNAL_ERROR, "This is a runtime exception")
        );
    }

}