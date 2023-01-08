package fr.ght1pc9kc.baywatch.teams.infra.mappers;

import fr.ght1pc9kc.baywatch.common.api.model.Entity;
import fr.ght1pc9kc.baywatch.dsl.tables.records.TeamsMembersRecord;
import fr.ght1pc9kc.baywatch.dsl.tables.records.TeamsRecord;
import fr.ght1pc9kc.baywatch.teams.api.model.Team;
import fr.ght1pc9kc.baywatch.teams.api.model.TeamMember;
import fr.ght1pc9kc.baywatch.teams.domain.model.PendingFor;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface TeamsMapper {
    Entity<Team> recordToTeam(TeamsRecord teamsRecord);

    TeamsRecord teamToRecord(Entity<Team> team);

    Entity<TeamMember> getMemberEntity(TeamsMembersRecord memberRecord);

    TeamsMembersRecord getTeamsMemberRecord(Entity<PendingFor> request);
}
