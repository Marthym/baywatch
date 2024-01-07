package fr.ght1pc9kc.baywatch.techwatch.infra.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import fr.ght1pc9kc.baywatch.common.infra.model.PatchOperation;
import fr.ght1pc9kc.baywatch.common.infra.model.PatchPayload;
import fr.ght1pc9kc.baywatch.common.infra.model.ResourcePatch;
import fr.ght1pc9kc.baywatch.techwatch.api.FeedService;
import fr.ght1pc9kc.baywatch.tests.samples.FeedSamples;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.net.URI;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class FeedControllerTest {
    private final ObjectMapper mapper = new ObjectMapper()
            .findAndRegisterModules();
    private FeedController tested;
    private FeedService mockFeedService;

    @BeforeEach
    void setUp() {
        mockFeedService = mock(FeedService.class);
        when(mockFeedService.delete(any())).thenReturn(Mono.just(1));
        tested = spy(new FeedController(mockFeedService, mapper));
        doReturn(Mono.just(ResponseEntity.ok(FeedSamples.JEDI))).when(tested).subscribe(any());
        doReturn(Mono.just(FeedSamples.SITH)).when(tested).update(anyString(), any());
    }

    @Test
    void should_patch_feeds() throws JsonProcessingException {
        PatchPayload payload = new PatchPayload(List.of(
                new ResourcePatch(PatchOperation.replace, null, URI.create("/feeds/42"), mapper.readTree("""
                            {
                                "name": "Test Feed",
                                "location": "https://www.jedi.com/obiwan"
                            }
                        """)),
                new ResourcePatch(PatchOperation.add, null, URI.create("/feeds"), mapper.readTree("""
                            {
                                "name": "Test Feed",
                                "location": "https://www.jedi.com/obiwan"
                            }
                        """)),
                new ResourcePatch(PatchOperation.remove, null, URI.create("/feeds/42"), null)
        ));
        StepVerifier.create(tested.bulkUpdate(payload))
                .expectNext(
                        URI.create("/feeds/" + FeedSamples.SITH.id()),
                        URI.create("/feeds/" + FeedSamples.JEDI.id()),
                        URI.create("/feeds/42"))
                .verifyComplete();

        verify(tested).subscribe(any());
        verify(tested).update(eq("42"), any());
        verify(mockFeedService).delete(List.of("42"));
    }
}