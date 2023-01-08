package fr.ght1pc9kc.baywatch.teams.infra.mappers;

import fr.ght1pc9kc.baywatch.common.api.model.Entity;
import fr.ght1pc9kc.baywatch.dsl.tables.records.TeamsRecord;
import fr.ght1pc9kc.baywatch.teams.api.model.Team;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface TeamsMapper {
    Entity<Team> recordToTeam(TeamsRecord teamsRecord);

    TeamsRecord teamToRecord(Entity<Team> team);
}
