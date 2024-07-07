package fr.ght1pc9kc.baywatch.teams.infra.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import fr.ght1pc9kc.baywatch.security.api.UserService;
import fr.ght1pc9kc.baywatch.tests.samples.UserSamples;
import fr.ght1pc9kc.juery.api.PageRequest;
import fr.ght1pc9kc.testy.core.extensions.WithObjectMapper;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

class UserMappingControllerTest {
    @RegisterExtension
    private static final WithObjectMapper wMapper = WithObjectMapper.builder()
            .build();

    private UserMappingController tested;

    @BeforeEach
    void setUp(ObjectMapper mapper) {
        UserService mockUserService = mock(UserService.class);
        doReturn(Mono.just(UserSamples.MWINDU)).when(mockUserService).get(anyString());
        doReturn(Flux.fromIterable(UserSamples.SAMPLES)).when(mockUserService).list(any(PageRequest.class));
        tested = new UserMappingController(mockUserService, mapper);
    }

    @Test
    void should_map_createdBy() {
        StepVerifier.create(tested.createdBy(Map.of("_createdBy", "okenobi")))
                .assertNext(actual -> SoftAssertions.assertSoftly(softly -> {
                    softly.assertThat(actual.get("_id")).isEqualTo(UserSamples.MWINDU.id());
                })).verifyComplete();
    }

    @Test
    void should_map_manager() {
        StepVerifier.create(tested.managers(Map.of("_id", "JEDI")))
                .assertNext(actual -> SoftAssertions.assertSoftly(softly -> {
                    softly.assertThat(actual.get("_id")).isEqualTo("US01GRQ11XKGHERDEBSCHBNJAY78");
                }))
                .expectNextCount(1)
                .verifyComplete();
    }

    @Test
    void should_map_teamMembers() {
        StepVerifier.create(tested.teamMembers(List.of(Map.of(
                        "_id", "JEDI",
                        "userId", "US01GRQ11XKGHERDEBSCHBNJAY78"
                ))))
                .assertNext(actual -> SoftAssertions.assertSoftly(softly -> {
                    softly.assertThat(actual.values().stream().findFirst().orElse(null)).containsKeys("_id");
                }))
                .verifyComplete();
    }
}