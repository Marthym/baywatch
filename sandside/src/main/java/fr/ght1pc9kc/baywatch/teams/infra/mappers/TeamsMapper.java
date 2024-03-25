package fr.ght1pc9kc.baywatch.teams.infra.mappers;

import fr.ght1pc9kc.baywatch.common.domain.DateUtils;
import fr.ght1pc9kc.baywatch.dsl.tables.Teams;
import fr.ght1pc9kc.baywatch.dsl.tables.TeamsMembers;
import fr.ght1pc9kc.baywatch.dsl.tables.records.TeamsMembersRecord;
import fr.ght1pc9kc.baywatch.dsl.tables.records.TeamsRecord;
import fr.ght1pc9kc.baywatch.teams.api.model.Team;
import fr.ght1pc9kc.baywatch.teams.api.model.TeamMember;
import fr.ght1pc9kc.baywatch.teams.domain.model.PendingFor;
import fr.ght1pc9kc.entity.api.Entity;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Optional;

import static fr.ght1pc9kc.baywatch.common.api.DefaultMeta.NO_ONE;
import static fr.ght1pc9kc.baywatch.common.api.DefaultMeta.createdAt;
import static fr.ght1pc9kc.baywatch.common.api.DefaultMeta.createdBy;

@Mapper(componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface TeamsMapper {
    default Entity<Team> getTeamEntity(TeamsRecord teamsRecord) {
        return Entity.identify(new Team(teamsRecord.getTeamName(), teamsRecord.getTeamTopic()))
                .meta(createdAt, Optional.ofNullable(teamsRecord.getTeamCreatedAt())
                        .map(DateUtils::toInstant).orElse(null))
                .meta(createdBy, teamsRecord.getTeamCreatedBy())
                .withId(teamsRecord.getTeamId());
    }

    default TeamsRecord getTeamRecord(Entity<Team> team) {
        return Teams.TEAMS.newRecord()
                .setTeamId(team.id())
                .setTeamCreatedAt(team.meta(createdAt, Instant.class)
                        .map(DateUtils::toLocalDateTime).orElse(LocalDateTime.MIN))
                .setTeamCreatedBy(team.meta(createdBy).orElse(NO_ONE))
                .setTeamName(team.self().name())
                .setTeamTopic(team.self().topic());
    }

    default Entity<TeamMember> getMemberEntity(TeamsMembersRecord teamsMembersRecord) {
        return Entity.identify(new TeamMember(
                        teamsMembersRecord.getTemeUserId(), PendingFor.from(teamsMembersRecord.getTemePendingFor())))
                .meta(createdAt, Optional.ofNullable(teamsMembersRecord.getTemeCreatedAt())
                        .map(DateUtils::toInstant).orElse(null))
                .meta(createdBy, teamsMembersRecord.getTemeCreatedBy())
                .withId(teamsMembersRecord.getTemeTeamId());
    }

    default TeamsMembersRecord getTeamsMemberRecord(Entity<TeamMember> request) {
        return TeamsMembers.TEAMS_MEMBERS.newRecord()
                .setTemeTeamId(request.id())
                .setTemeCreatedAt(request.meta(createdAt, Instant.class)
                        .map(DateUtils::toLocalDateTime).orElse(LocalDateTime.MIN))
                .setTemeCreatedBy(request.meta(createdBy).orElse(NO_ONE))
                .setTemePendingFor(request.self().pending().value())
                .setTemeUserId(request.self().userId());
    }
}
