package fr.ght1pc9kc.baywatch.api.model.request.filter;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

class CriteriaTest {
    @Test
    void should_and_none_criteria() {
        {
            Criteria expected = Criteria.property("obiwan").eq("kenobi");
            Criteria actual = Criteria.and(Criteria.none(), expected);

            Assertions.assertThat(actual).isEqualTo(expected);
        }
        {
            Criteria expected = Criteria.none();
            Criteria actual = Criteria.and(Criteria.none(), expected);

            Assertions.assertThat(actual).isEqualTo(expected);
        }
    }

    @Test
    void should_or_none_criteria() {
        {
            Criteria expected = Criteria.property("obiwan").eq("kenobi");
            Criteria actual = Criteria.or(Criteria.none(), expected);

            Assertions.assertThat(actual).isEqualTo(expected);
        }
        {
            Criteria expected = Criteria.none();
            Criteria actual = Criteria.or(Criteria.none(), expected);

            Assertions.assertThat(actual).isEqualTo(expected);
        }
    }

    @Test
    void should_not_none_criteria() {
        Criteria actual = Criteria.not(Criteria.none());
        Assertions.assertThat(actual).isEqualTo(Criteria.none());
    }

    @Test
    void should_and_multiple_criteria() {
        {
            Criteria actual = Criteria.and();
            Assertions.assertThat(actual).isEqualTo(Criteria.none());
        }
        {
            Criteria actual = Criteria.and(
                    Criteria.property("obiwan").eq("kenobi"),
                    Criteria.property("luke").eq("skywalker"),
                    Criteria.property("obiwan").eq("kenobi"),
                    Criteria.property("leia").eq("organa")
            );
            Assertions.assertThat(actual).isInstanceOf(AndOperation.class);
            Assertions.assertThat(((AndOperation) actual).andCriteria).containsOnly(
                    Criteria.property("obiwan").eq("kenobi"),
                    Criteria.property("luke").eq("skywalker"),
                    Criteria.property("leia").eq("organa")
            );
        }
    }

    @Test
    void should_or_multiple_criteria() {
        {
            Criteria actual = Criteria.or();
            Assertions.assertThat(actual).isEqualTo(Criteria.none());
        }
        {
            Criteria actual = Criteria.or(
                    Criteria.property("obiwan").eq("kenobi"),
                    Criteria.property("luke").eq("skywalker"),
                    Criteria.property("obiwan").eq("kenobi"),
                    Criteria.property("leia").eq("organa")
            );
            Assertions.assertThat(actual).isInstanceOf(OrOperation.class);
            Assertions.assertThat(((OrOperation) actual).orCriteria).containsOnly(
                    Criteria.property("obiwan").eq("kenobi"),
                    Criteria.property("luke").eq("skywalker"),
                    Criteria.property("leia").eq("organa")
            );
        }
    }

    @Test
    void should_reverse_equal_and() {
        Criteria left = Criteria.property("name").eq("obiwan");
        Criteria right = Criteria.property("force").eq("light");

        Assertions.assertThat(left.and(right)).isEqualTo(right.and(left));
    }

    @Test
    void should_reverse_equal_or() {
        Criteria left = Criteria.property("name").eq("obiwan");
        Criteria right = Criteria.property("force").eq("light");

        Assertions.assertThat(left.or(right)).isEqualTo(right.or(left));
    }
}