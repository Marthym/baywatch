package fr.ght1pc9kc.baywatch.infra.mappers;

import fr.ght1pc9kc.baywatch.api.model.EntitiesProperties;
import fr.ght1pc9kc.baywatch.api.model.Flags;
import lombok.experimental.UtilityClass;
import org.jooq.Field;
import org.jooq.impl.DSL;

import java.util.Map;

import static fr.ght1pc9kc.baywatch.dsl.tables.Feeds.FEEDS;
import static fr.ght1pc9kc.baywatch.dsl.tables.FeedsUsers.FEEDS_USERS;
import static fr.ght1pc9kc.baywatch.dsl.tables.News.NEWS;
import static fr.ght1pc9kc.baywatch.dsl.tables.NewsUserState.NEWS_USER_STATE;
import static fr.ght1pc9kc.baywatch.dsl.tables.Users.USERS;

@UtilityClass
public final class PropertiesMappers {
    public static final Map<String, Field<?>> FEEDS_PROPERTIES_MAPPING = Map.of(
            EntitiesProperties.ID, FEEDS.FEED_ID,
            EntitiesProperties.NAME, FEEDS.FEED_NAME,
            EntitiesProperties.URL, FEEDS.FEED_URL,
            EntitiesProperties.LAST_WATCH, FEEDS.FEED_LAST_WATCH,
            EntitiesProperties.USER_ID, FEEDS_USERS.FEUS_USER_ID
    );
    public static final Map<String, Field<?>> NEWS_PROPERTIES_MAPPING = Map.of(
            EntitiesProperties.ID, NEWS.NEWS_ID,
            EntitiesProperties.DESCRIPTION, NEWS.NEWS_DESCRIPTION,
            EntitiesProperties.LINK, NEWS.NEWS_LINK,
            EntitiesProperties.PUBLICATION, NEWS.NEWS_PUBLICATION,
            EntitiesProperties.READ, DSL.coalesce(NEWS_USER_STATE.NURS_STATE, Flags.NONE).bitAnd(Flags.READ),
            EntitiesProperties.SHARED, DSL.coalesce(NEWS_USER_STATE.NURS_STATE, Flags.NONE).bitAnd(Flags.SHARED),
            EntitiesProperties.TITLE, NEWS.NEWS_TITLE
    );
    public static final Map<String, Field<?>> STATE_PROPERTIES_MAPPING = Map.of(
            EntitiesProperties.NEWS_ID, NEWS_USER_STATE.NURS_NEWS_ID,
            EntitiesProperties.USER_ID, NEWS_USER_STATE.NURS_USER_ID,
            EntitiesProperties.STATE, NEWS_USER_STATE.NURS_STATE
    );
    public static final Map<String, Field<?>> USER_PROPERTIES_MAPPING = Map.of(
            EntitiesProperties.ID, USERS.USER_ID,
            EntitiesProperties.LOGIN, USERS.USER_LOGIN,
            EntitiesProperties.NAME, USERS.USER_NAME,
            EntitiesProperties.MAIL, USERS.USER_EMAIL
    );
}
