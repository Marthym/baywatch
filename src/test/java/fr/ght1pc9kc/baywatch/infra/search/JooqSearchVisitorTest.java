package fr.ght1pc9kc.baywatch.infra.search;

import fr.ght1pc9kc.baywatch.api.model.search.Criteria;
import fr.ght1pc9kc.baywatch.dsl.tables.News;
import fr.ght1pc9kc.baywatch.infra.mappers.NewsToRecordConverter;
import org.jooq.Condition;
import org.jooq.conf.ParamType;
import org.jooq.impl.DSL;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.time.LocalDateTime;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

class JooqSearchVisitorTest {

    private static final LocalDateTime NOW = LocalDateTime.parse("2020-12-11T10:20:42");

    private final JooqSearchVisitor tested = new JooqSearchVisitor(NewsToRecordConverter.PROPERTIES_MAPPING::get);

    @ParameterizedTest
    @MethodSource("provideSCriteria")
    void should_create_condition_from_criteria(Criteria criteria, String expected) {
        Condition actual = criteria.visit(tested);
        assertThat(DSL.select(News.NEWS.NEWS_ID).where(actual).getSQL(ParamType.INLINED)).isEqualTo(expected);
    }

    private static Stream<Arguments> provideSCriteria() {
        return Stream.of(
                Arguments.of(Criteria.property("title").eq("Obiwan")
                                .and(Criteria.property("publication").gt(NOW)
                                        .or(Criteria.property("publication").lt(NOW))),
                        "select \"PUBLIC\".\"NEWS\".\"NEWS_ID\" "
                                + "where ((\"PUBLIC\".\"NEWS\".\"NEWS_PUBLICATION\" < timestamp '2020-12-11 10:20:42.0' "
                                + "or \"PUBLIC\".\"NEWS\".\"NEWS_PUBLICATION\" > timestamp '2020-12-11 10:20:42.0') "
                                + "and \"PUBLIC\".\"NEWS\".\"NEWS_TITLE\" = 'Obiwan')"),
                Arguments.of(Criteria.property("title").eq("Obiwan")
                                .and(Criteria.property("publication").gt(NOW))
                                .or(Criteria.property("publication").lt(NOW)),
                        "select \"PUBLIC\".\"NEWS\".\"NEWS_ID\" "
                                + "where (\"PUBLIC\".\"NEWS\".\"NEWS_PUBLICATION\" < timestamp '2020-12-11 10:20:42.0' "
                                + "or (\"PUBLIC\".\"NEWS\".\"NEWS_PUBLICATION\" > timestamp '2020-12-11 10:20:42.0' "
                                + "and \"PUBLIC\".\"NEWS\".\"NEWS_TITLE\" = 'Obiwan'))")
        );
    }
}