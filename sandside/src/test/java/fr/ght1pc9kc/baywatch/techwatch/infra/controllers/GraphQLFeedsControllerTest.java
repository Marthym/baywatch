package fr.ght1pc9kc.baywatch.techwatch.infra.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import fr.ght1pc9kc.baywatch.common.infra.adapters.GraphqlExceptionAdapter;
import fr.ght1pc9kc.baywatch.common.infra.config.GraphqlConfiguration;
import fr.ght1pc9kc.baywatch.common.infra.config.jackson.JacksonMappingConfiguration;
import fr.ght1pc9kc.baywatch.techwatch.api.FeedService;
import fr.ght1pc9kc.baywatch.techwatch.api.PopularNewsService;
import fr.ght1pc9kc.baywatch.techwatch.infra.MockSecurityConfiguration;
import fr.ght1pc9kc.baywatch.techwatch.infra.config.GraphqlTechwatchConfig;
import fr.ght1pc9kc.baywatch.tests.samples.FeedSamples;
import fr.ght1pc9kc.juery.api.PageRequest;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.graphql.GraphQlTest;
import org.springframework.context.annotation.Import;
import org.springframework.graphql.execution.ErrorType;
import org.springframework.graphql.test.tester.GraphQlTester;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;

import static net.javacrumbs.jsonunit.assertj.JsonAssertions.assertThatJson;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

@Tag("integration")
@ActiveProfiles("test")
@Import({JacksonMappingConfiguration.class, MockSecurityConfiguration.class, GraphqlConfiguration.class, GraphqlTechwatchConfig.class,
        GraphqlExceptionAdapter.class})
@GraphQlTest(GraphQLFeedsController.class)
class GraphQLFeedsControllerTest {

    @MockitoBean
    FeedService mockFeedService;

    @MockitoBean
    PopularNewsService mockPopularNewsService;

    @Autowired
    private GraphQlTester gqlClient;

    @Autowired
    ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        when(mockFeedService.get(anyString()))
                .thenReturn(Mono.just(FeedSamples.JEDI));
        when(mockFeedService.list(any(PageRequest.class)))
                .thenReturn(Flux.just(FeedSamples.JEDI));
        when(mockFeedService.count(any(PageRequest.class))).thenReturn(Mono.just(1));
        doReturn(Flux.just(FeedSamples.JEDI)).when(mockFeedService).addAndSubscribe(anyCollection());
    }

    @Test
    void should_call_get() throws JsonProcessingException {
        Object response = gqlClient.documentName("feedsServiceTest").operationName("GetFeed")
                .variable("feedId", FeedSamples.JEDI.id())
                .execute().path("getFeed")
                .entity(Object.class).get();

        String actual = objectMapper.writeValueAsString(response);
        Assertions.assertThat(actual).isNotBlank();
        assertThatJson(actual)
                .isObject()
                .containsKeys("_id", "name", "location", "tags");
    }

    @Test
    void should_call_feeds_search() throws JsonProcessingException {
        Object response = gqlClient.documentName("feedsServiceTest").operationName("SearchFeedsQuery")
                .variable("_p", 0)
                .variable("_pp", 20)
                .variable("_s", "name")
                .execute().path("feedsSearch")
                .entity(Object.class)
                .get();

        String actual = objectMapper.writeValueAsString(response);
        Assertions.assertThat(actual).isNotBlank();
        assertThatJson(actual)
                .isObject()
                .containsEntry("totalCount", 1)
                .containsKey("entities")
                .node("entities")
                .isArray().first().isObject()
                .containsKeys("_id", "name", "location", "tags");
    }

    @Test
    @WithMockUser(username = "admin", roles = {"USER", "ADMIN"})
    void should_call_feedAddAndSubscribe() throws JsonProcessingException {
        gqlClient.documentName("feedsServiceTest")
                .operationName("FeedAddAndSubscribe")
                .variable("feed", null)
                .execute().errors().expect(ex -> ex.getErrorType() == ErrorType.INTERNAL_ERROR);

        GraphQlTester.Response executed = gqlClient.documentName("feedsServiceTest")
                .operationName("FeedAddAndSubscribe")
                .variable("feed", Map.of(
                        "name", "Jedi.com",
                        "location", "https://jedi.com/atom.xml",
                        "description", "Jedi News Feed",
                        "tags", List.of("light", "good")))
                .execute();

        Object response = executed.path("feedAddAndSubscribe")
                .entity(Object.class)
                .get();
        String actual = objectMapper.writeValueAsString(response);
        Assertions.assertThat(actual).isNotBlank();
        assertThatJson(actual)
                .isObject()
                .containsOnly(
                        Map.entry("_id", FeedSamples.JEDI.id()),
                        Map.entry("name", FeedSamples.JEDI.self().name())
                );
    }
}