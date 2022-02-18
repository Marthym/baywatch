package fr.ght1pc9kc.baywatch.infra.common;

import fr.ght1pc9kc.baywatch.api.techwatch.model.Flags;
import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;
import org.junit.jupiter.api.Test;

import static fr.ght1pc9kc.baywatch.dsl.tables.NewsUserState.NEWS_USER_STATE;

public class JooqHelperTest {
    @Test
    void should_extract_field() {
        Condition tested = DSL.coalesce(NEWS_USER_STATE.NURS_STATE, Flags.NONE).bitAnd(Flags.READ).eq(1)
                .and(NEWS_USER_STATE.NURS_NEWS_ID.isNotNull());

        DSLContext dsl = DSL.using(SQLDialect.H2);
        dsl.renderContext().visit(tested).render();
        DSL.select(NEWS_USER_STATE.NURS_NEWS_ID).from(NEWS_USER_STATE).getQuery();
    }
}
