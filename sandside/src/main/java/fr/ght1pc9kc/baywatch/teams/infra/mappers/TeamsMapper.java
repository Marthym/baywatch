package fr.ght1pc9kc.baywatch.teams.infra.mappers;

import fr.ght1pc9kc.baywatch.common.api.model.Entity;
import fr.ght1pc9kc.baywatch.common.domain.DateUtils;
import fr.ght1pc9kc.baywatch.dsl.tables.records.TeamsMembersRecord;
import fr.ght1pc9kc.baywatch.dsl.tables.records.TeamsRecord;
import fr.ght1pc9kc.baywatch.teams.api.model.Team;
import fr.ght1pc9kc.baywatch.teams.api.model.TeamMember;
import fr.ght1pc9kc.baywatch.teams.domain.model.PendingFor;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.time.Instant;
import java.time.LocalDateTime;

@Mapper(componentModel = "spring")
public interface TeamsMapper {
    @Mapping(source = "teamId", target = "id")
    @Mapping(source = "teamCreatedAt", target = "createdAt")
    @Mapping(source = "teamCreatedBy", target = "createdBy")
    @Mapping(source = "teamName", target = "self.name")
    @Mapping(source = "teamTopic", target = "self.topic")
    Entity<Team> getTeamEntity(TeamsRecord teamsRecord);

    TeamsRecord teamToRecord(Entity<Team> team);

    Entity<TeamMember> getMemberEntity(TeamsMembersRecord memberRecord);

    TeamsMembersRecord getTeamsMemberRecord(Entity<PendingFor> request);

    default Instant fromLocalDateTime(LocalDateTime date) {
        return DateUtils.toInstant(date);
    }
}
