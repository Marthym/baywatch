package fr.ght1pc9kc.baywatch.teams.infra.mappers;

import fr.ght1pc9kc.baywatch.common.api.model.Entity;
import fr.ght1pc9kc.baywatch.common.domain.DateUtils;
import fr.ght1pc9kc.baywatch.dsl.tables.records.TeamsMembersRecord;
import fr.ght1pc9kc.baywatch.dsl.tables.records.TeamsRecord;
import fr.ght1pc9kc.baywatch.teams.api.model.Team;
import fr.ght1pc9kc.baywatch.teams.api.model.TeamMember;
import org.mapstruct.InheritInverseConfiguration;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import java.time.Instant;
import java.time.LocalDateTime;

@Mapper(componentModel = "spring",
        imports = {DateUtils.class},
        unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface TeamsMapper {
    @Mapping(source = "teamId", target = "id")
    @Mapping(source = "teamCreatedAt", target = "createdAt")
    @Mapping(source = "teamCreatedBy", target = "createdBy")
    @Mapping(source = "teamName", target = "self.name")
    @Mapping(source = "teamTopic", target = "self.topic")
    Entity<Team> getTeamEntity(TeamsRecord teamsRecord);

    @InheritInverseConfiguration
    @Mapping(target = "teamCreatedAt", expression = "java( DateUtils.toLocalDateTime(team.createdAt) )")
    TeamsRecord getTeamRecord(Entity<Team> team);

    @Mapping(source = "temeTeamId", target = "id")
    @Mapping(source = "temeCreatedAt", target = "createdAt")
    @Mapping(source = "temeCreatedBy", target = "createdBy")
    @Mapping(source = "temeUserId", target = "self.userId")
    @Mapping(target = "self.pending", expression = "java( PendingFor.from(teamsMembersRecord.getTemePendingFor()) )")
    Entity<TeamMember> getMemberEntity(TeamsMembersRecord teamsMembersRecord);

    @InheritInverseConfiguration
    @Mapping(target = "temePendingFor", expression = "java( request.self.pending().value() )")
    @Mapping(target = "temeCreatedAt", expression = "java( DateUtils.toLocalDateTime(request.createdAt) )")
    TeamsMembersRecord getTeamsMemberRecord(Entity<TeamMember> request);

    default Instant fromLocalDateTime(LocalDateTime date) {
        return DateUtils.toInstant(date);
    }
}
