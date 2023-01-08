package fr.ght1pc9kc.baywatch.teams.infra.mappers;

import fr.ght1pc9kc.baywatch.common.api.model.EntitiesProperties;
import lombok.experimental.UtilityClass;
import org.jooq.Field;

import java.util.Map;

import static fr.ght1pc9kc.baywatch.dsl.tables.Teams.TEAMS;
import static fr.ght1pc9kc.baywatch.dsl.tables.TeamsMembers.TEAMS_MEMBERS;

@UtilityClass
public class PropertiesMapper {
    public static final Map<String, Field<?>> TEAMS_PROPERTIES_MAPPING = Map.of(
            EntitiesProperties.ID, TEAMS.TEAM_ID,
            EntitiesProperties.NAME, TEAMS.TEAM_NAME,
            EntitiesProperties.TOPIC, TEAMS.TEAM_TOPIC
    );

    public static final Map<String, Field<?>> TEAMS_MEMBERS_PROPERTIES_MAPPING = Map.of(
            EntitiesProperties.ID, TEAMS_MEMBERS.TEME_TEAM_ID,
            EntitiesProperties.USER_ID, TEAMS_MEMBERS.TEME_USER_ID,
            EntitiesProperties.PENDING_FOR, TEAMS_MEMBERS.TEME_PENDING_FOR
    );
}
