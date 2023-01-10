package fr.ght1pc9kc.baywatch.teams.infra.samples;

import fr.ght1pc9kc.baywatch.dsl.tables.records.TeamsMembersRecord;
import fr.ght1pc9kc.baywatch.teams.domain.model.PendingFor;
import fr.ght1pc9kc.baywatch.tests.samples.UserSamples;
import fr.ght1pc9kc.baywatch.tests.samples.infra.UsersRecordSamples;
import fr.ght1pc9kc.testy.jooq.model.RelationalDataSet;

import java.time.LocalDateTime;
import java.util.List;

import static fr.ght1pc9kc.baywatch.dsl.tables.TeamsMembers.TEAMS_MEMBERS;

public class TeamsMembersRecordSamples implements RelationalDataSet<TeamsMembersRecord> {
    public static final RelationalDataSet<TeamsMembersRecord> SAMPLE = new TeamsMembersRecordSamples();

    @Override
    public List<TeamsMembersRecord> records() {
        return List.of(
                TEAMS_MEMBERS.newRecord()
                        .setTemeTeamId(TeamsRecordSamples.JEDI_TEAM.getTeamId())
                        .setTemeUserId(UserSamples.LUKE.id)
                        .setTemeCreatedBy(UserSamples.LUKE.id)
                        .setTemeCreatedAt(LocalDateTime.parse("2023-01-10T22:52:42"))
                        .setTemePendingFor(PendingFor.NONE.value()),
                TEAMS_MEMBERS.newRecord()
                        .setTemeTeamId(TeamsRecordSamples.JEDI_TEAM.getTeamId())
                        .setTemeUserId(UserSamples.OBIWAN.id)
                        .setTemeCreatedBy(UserSamples.OBIWAN.id)
                        .setTemeCreatedAt(LocalDateTime.parse("2023-01-12T22:52:42"))
                        .setTemePendingFor(PendingFor.NONE.value()),
                TEAMS_MEMBERS.newRecord()
                        .setTemeTeamId(TeamsRecordSamples.JEDI_TEAM.getTeamId())
                        .setTemeUserId(UserSamples.YODA.id)
                        .setTemeCreatedBy(UsersRecordSamples.LSKYWALKER.getUserId())
                        .setTemeCreatedAt(LocalDateTime.parse("2023-01-14T22:52:42"))
                        .setTemePendingFor(PendingFor.NONE.value())
        );
    }
}
