package fr.ght1pc9kc.baywatch.infra.search;

import fr.ght1pc9kc.baywatch.api.model.search.Criteria;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

class StringSearchVisitorTest {
    public StringSearchVisitor tested = new StringSearchVisitor();

    @ParameterizedTest
    @MethodSource(value = "provideSCriteria")
    void should_consume_criteria_as_string(Criteria criteria, String expected) {
        String actual = criteria.visit(tested);
        assertThat(actual).isEqualTo(expected);
    }

    private static Stream<Arguments> provideSCriteria() {
        return Stream.of(
                Arguments.of(Criteria.property("jedi").eq("Obiwan")
                                .and(Criteria.property("age").gt(40).or(Criteria.property("age").lt(20))),
                        "(jedi = 'Obiwan' and (age > 40 or age < 20))"),
                Arguments.of(Criteria.property("jedi").in("Obiwan", "Anakin", "Luke")
                                .and(Criteria.property("age").gt(40).or(Criteria.property("age").lt(20))),
                        "(jedi in [Obiwan, Anakin, Luke] and (age > 40 or age < 20))"),
                Arguments.of(Criteria.property("jedi").eq("Obiwan")
                                .and(Criteria.property("age").gt(40))
                                .or(Criteria.property("age").lt(20)),
                        "((jedi = 'Obiwan' and age > 40) or age < 20)")
        );
    }
}