package fr.ght1pc9kc.baywatch.common.infra.mappers;

import fr.ght1pc9kc.baywatch.common.api.model.EntitiesProperties;
import fr.ght1pc9kc.baywatch.dsl.tables.NewsUserState;
import fr.ght1pc9kc.baywatch.techwatch.api.model.Flags;
import lombok.experimental.UtilityClass;
import org.jooq.Field;
import org.jooq.impl.DSL;

import java.util.Map;

import static fr.ght1pc9kc.baywatch.common.api.model.EntitiesProperties.TAGS_SEPARATOR;
import static fr.ght1pc9kc.baywatch.dsl.tables.Feeds.FEEDS;
import static fr.ght1pc9kc.baywatch.dsl.tables.FeedsUsers.FEEDS_USERS;
import static fr.ght1pc9kc.baywatch.dsl.tables.News.NEWS;
import static fr.ght1pc9kc.baywatch.dsl.tables.NewsFeeds.NEWS_FEEDS;
import static fr.ght1pc9kc.baywatch.dsl.tables.NewsUserState.NEWS_USER_STATE;
import static fr.ght1pc9kc.baywatch.dsl.tables.Users.USERS;
import static fr.ght1pc9kc.baywatch.dsl.tables.UsersRoles.USERS_ROLES;

@UtilityClass
public final class PropertiesMappers {
    public static final NewsUserState POPULAR = NEWS_USER_STATE.as("POPULAR");
    public static final NewsUserState NEWS_STATE = NEWS_USER_STATE.as("NEWS_STATE");

    public static final Map<String, Field<?>> FEEDS_PROPERTIES_MAPPING = Map.of(
            EntitiesProperties.ID, FEEDS.FEED_ID,
            EntitiesProperties.NAME, FEEDS.FEED_NAME,
            EntitiesProperties.URL, FEEDS.FEED_URL,
            EntitiesProperties.LAST_WATCH, FEEDS.FEED_LAST_WATCH,
            EntitiesProperties.USER_ID, FEEDS_USERS.FEUS_USER_ID,
            EntitiesProperties.FEED_ID, FEEDS_USERS.FEUS_FEED_ID,
            EntitiesProperties.TAGS, DSL.concat(DSL.value(TAGS_SEPARATOR), FEEDS_USERS.FEUS_TAGS, DSL.value(TAGS_SEPARATOR))
    );

    public static final Map<String, Field<?>> NEWS_PROPERTIES_MAPPING = Map.of(
            EntitiesProperties.ID, NEWS.NEWS_ID,
            EntitiesProperties.NEWS_ID, NEWS.NEWS_ID,
            EntitiesProperties.DESCRIPTION, NEWS.NEWS_DESCRIPTION,
            EntitiesProperties.LINK, NEWS.NEWS_LINK,
            EntitiesProperties.PUBLICATION, NEWS.NEWS_PUBLICATION,
            EntitiesProperties.FEED_ID, NEWS_FEEDS.NEFE_FEED_ID,
            EntitiesProperties.READ, DSL.coalesce(NEWS_STATE.NURS_STATE, Flags.NONE).bitAnd(Flags.READ),
            EntitiesProperties.KEEP, DSL.field(DSL.coalesce(NEWS_STATE.NURS_STATE, Flags.NONE)
                    .bitAnd(Flags.KEEP).eq(Flags.KEEP)),
            EntitiesProperties.POPULAR, DSL.field(DSL.coalesce(POPULAR.NURS_STATE, Flags.NONE)
                    .bitAnd(Flags.SHARED).eq(Flags.SHARED)),
            EntitiesProperties.TITLE, NEWS.NEWS_TITLE
    );

    public static final Map<String, Field<?>> STATE_PROPERTIES_MAPPING = Map.of(
            EntitiesProperties.NEWS_ID, NEWS_USER_STATE.NURS_NEWS_ID,
            EntitiesProperties.USER_ID, NEWS_USER_STATE.NURS_USER_ID,
            EntitiesProperties.SHARED, DSL.field(DSL.coalesce(NEWS_USER_STATE.NURS_STATE, Flags.NONE)
                    .bitAnd(Flags.SHARED).eq(Flags.SHARED))
    );

    public static final Map<String, Field<?>> USER_PROPERTIES_MAPPING = Map.of(
            EntitiesProperties.ID, USERS.USER_ID,
            EntitiesProperties.CREATED_AT, USERS.USER_CREATED_AT,
            EntitiesProperties.LOGIN, USERS.USER_LOGIN,
            EntitiesProperties.NAME, USERS.USER_NAME,
            EntitiesProperties.MAIL, USERS.USER_EMAIL,
            EntitiesProperties.ROLES, USERS_ROLES.USRO_ROLE
    );
}
