package fr.ght1pc9kc.baywatch.infra.adapters.persistence;

import fr.ght1pc9kc.baywatch.api.model.EntitiesProperties;
import fr.ght1pc9kc.baywatch.dsl.Tables;
import fr.ght1pc9kc.juery.jooq.filter.JooqConditionVisitor;
import lombok.experimental.UtilityClass;
import org.jooq.impl.DSL;

import java.util.Map;

import static fr.ght1pc9kc.baywatch.dsl.tables.FeedsUsers.FEEDS_USERS;

@UtilityClass
public class FeedConditionsVisitors {
    private static final JooqConditionVisitor FEED_ID_VISITOR = new JooqConditionVisitor(
            Map.of(EntitiesProperties.ID, Tables.FEEDS.FEED_ID));
    private static final JooqConditionVisitor FEED_USERS_ID_VISITOR = new JooqConditionVisitor(
            Map.of(EntitiesProperties.FEED_ID, Tables.FEEDS_USERS.FEUS_FEED_ID));

    private static final JooqConditionVisitor FEED_USERS_HAVING_VISITOR = new JooqConditionVisitor(
            Map.of(EntitiesProperties.COUNT, DSL.count(FEEDS_USERS.FEUS_USER_ID)));

    public static JooqConditionVisitor feedIdVisitor() {
        return FEED_ID_VISITOR;
    }

    public static JooqConditionVisitor feedUserIdVisitor() {
        return FEED_USERS_ID_VISITOR;
    }

    public static JooqConditionVisitor feedUserHavingVisitor() {
        return FEED_USERS_HAVING_VISITOR;
    }

}
