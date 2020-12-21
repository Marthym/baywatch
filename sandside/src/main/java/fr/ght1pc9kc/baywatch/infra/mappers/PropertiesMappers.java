package fr.ght1pc9kc.baywatch.infra.mappers;

import lombok.experimental.UtilityClass;
import org.jooq.Field;

import java.util.Map;

import static fr.ght1pc9kc.baywatch.dsl.tables.Feeds.FEEDS;
import static fr.ght1pc9kc.baywatch.dsl.tables.News.NEWS;
import static fr.ght1pc9kc.baywatch.dsl.tables.NewsUserState.NEWS_USER_STATE;
import static fr.ght1pc9kc.baywatch.dsl.tables.Users.USERS;

@UtilityClass
public final class PropertiesMappers {
    public static final String DESCRIPTION = "description";
    public static final String ID = "id";
    public static final String LAST_WATCH = "lastWatch";
    public static final String LINK = "link";
    public static final String LOGIN = "login";
    public static final String MAIL = "mail";
    public static final String NAME = "name";
    public static final String NEWS_ID = "newsId";
    public static final String PUBLICATION = "publication";
    public static final String STATE = "state";
    public static final String TITLE = "title";
    public static final String URL = "url";
    public static final String USER_ID = "userId";

    public static final Map<String, Field<?>> FEEDS_PROPERTIES_MAPPING = Map.of(
            ID, FEEDS.FEED_ID,
            NAME, FEEDS.FEED_NAME,
            URL, FEEDS.FEED_URL,
            LAST_WATCH, FEEDS.FEED_LAST_WATCH
    );
    public static final Map<String, Field<?>> NEWS_PROPERTIES_MAPPING = Map.of(
            ID, NEWS.NEWS_ID,
            DESCRIPTION, NEWS.NEWS_DESCRIPTION,
            LINK, NEWS.NEWS_LINK,
            PUBLICATION, NEWS.NEWS_PUBLICATION,
            TITLE, NEWS.NEWS_TITLE
    );
    public static final Map<String, Field<?>> STATE_PROPERTIES_MAPPING = Map.of(
            NEWS_ID, NEWS_USER_STATE.NURS_NEWS_ID,
            USER_ID, NEWS_USER_STATE.NURS_USER_ID,
            STATE, NEWS_USER_STATE.NURS_STATE
    );
    public static final Map<String, Field<?>> USER_PROPERTIES_MAPPING = Map.of(
            ID, USERS.USER_ID,
            LOGIN, USERS.USER_LOGIN,
            NAME, USERS.USER_NAME,
            MAIL, USERS.USER_EMAIL
    );
}
