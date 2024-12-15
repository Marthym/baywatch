package fr.ght1pc9kc.baywatch.common.domain;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class QueryContextTest {
    @Test
    void should_check_if_scoped() {
        assertThat(QueryContext.id("42")).extracting(QueryContext::isScoped).isEqualTo(false);
    }
}