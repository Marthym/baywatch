package fr.ght1pc9kc.baywatch.teams.infra.mappers;

import fr.ght1pc9kc.baywatch.common.api.model.EntitiesProperties;
import lombok.experimental.UtilityClass;
import org.jooq.Field;

import java.util.Map;

import static fr.ght1pc9kc.baywatch.dsl.tables.Teams.TEAMS;

@UtilityClass
public class PropertiesMapper {
    public static final Map<String, Field<?>> TEAMS_PROPERTIES_MAPPING = Map.of(
            EntitiesProperties.ID, TEAMS.TEAM_ID,
            EntitiesProperties.NAME, TEAMS.TEAM_NAME,
            EntitiesProperties.TOPIC, TEAMS.TEAM_TOPIC
    );
}
