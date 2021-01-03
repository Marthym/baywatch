package fr.ght1pc9kc.baywatch.infra.request;

import fr.ght1pc9kc.baywatch.api.model.request.PageRequest;
import fr.ght1pc9kc.baywatch.api.model.request.filter.Criteria;
import fr.ght1pc9kc.baywatch.api.model.request.pagination.Direction;
import fr.ght1pc9kc.baywatch.api.model.request.pagination.Order;
import fr.ght1pc9kc.baywatch.api.model.request.pagination.Sort;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.net.URI;
import java.util.Map;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.fail;

class PageRequestFormatterTest {
    @MethodSource
    @ParameterizedTest
    void should_parse_page_request_from_map(String qs, Map<String, String> queryString, PageRequest expected) {
        PageRequest actual = PageRequestFormatter.parse(queryString);

        Assertions.assertThat(actual).isEqualTo(expected);
    }

    @SuppressWarnings("unused")
    private static Stream<Arguments> should_parse_page_request_from_map() {
        return Stream.of(
                Arguments.of(
                        "_p=2&_s=name,-email&profile=jedi&job=master",
                        Map.of(
                                "_p", "2",
                                "_s", "name, -email",
                                "profile", "jedi",
                                "job", "master"),
                        PageRequest.builder()
                                .page(2).size(100)
                                .filter(Criteria.property("profile").eq("jedi").and(Criteria.property("job").eq("master")))
                                .sort(Sort.of(new Order(Direction.ASC, "name"), new Order(Direction.DESC, "email")))
                                .build()),
                Arguments.of(
                        "_pp=200",
                        Map.of("_pp", "200"),
                        PageRequest.builder()
                                .page(0).size(100)
                                .filter(Criteria.none())
                                .sort(Sort.of())
                                .build()),
                Arguments.of(
                        "_pp=200&name",
                        Map.of(
                                "_pp", "200",
                                "name", ""),
                        PageRequest.builder()
                                .page(0).size(100)
                                .filter(Criteria.property("name").eq(true))
                                .sort(Sort.of())
                                .build())
        );
    }

    @MethodSource
    @ParameterizedTest
    void should_parse_sort_parameter(String sortValue, Sort expected) {
        Sort actual = PageRequestFormatter.parseSortParameter(sortValue);
        Assertions.assertThat(actual).isEqualTo(expected);
    }

    @SuppressWarnings("unused")
    private static Stream<Arguments> should_parse_sort_parameter() {
        return Stream.of(
                Arguments.of("+name,-mail,+karma", Sort.of(Order.asc("name"), Order.desc("mail"), Order.asc("karma"))),
                Arguments.of("name,mail,karma", Sort.of(Order.asc("name"), Order.asc("mail"), Order.asc("karma"))),
                Arguments.of("+name,,+karma", Sort.of(Order.asc("name"), Order.asc("karma"))),
                Arguments.of(",+name,+karma,", Sort.of(Order.asc("name"), Order.asc("karma"))),
                Arguments.of("", Sort.of()),
                Arguments.of("-", Sort.of()),
                Arguments.of("   ", Sort.of()),
                Arguments.of(",,,", Sort.of())
        );
    }

    @Test
    void should_parse_page_request_from_string() {
        fail();
    }

    @MethodSource
    @ParameterizedTest
    void should_format_page_request_to_query_string(String expected, PageRequest pr) {
        String actual = PageRequestFormatter.formatPageRequest(pr);

        Assertions.assertThat(actual).isEqualTo(expected);
    }

    @SuppressWarnings("unused")
    private static Stream<Arguments> should_format_page_request_to_query_string() {
        return Stream.of(
                Arguments.of(
                        "_p=2&_s=name,-email&profile=jedi&job=master",
                        PageRequest.builder()
                                .page(2).size(100)
                                .filter(Criteria.property("profile").eq("jedi").and(Criteria.property("job").eq("master")))
                                .sort(Sort.of(new Order(Direction.ASC, "name"), new Order(Direction.DESC, "email")))
                                .build()),
                Arguments.of(
                        "",
                        PageRequest.builder()
                                .page(0).size(100)
                                .filter(Criteria.none())
                                .sort(Sort.of())
                                .build()),
                Arguments.of(
                        "name=true",
                        PageRequest.builder()
                                .page(0).size(100)
                                .filter(Criteria.property("name").eq(true))
                                .sort(Sort.of())
                                .build())
        );
    }
}