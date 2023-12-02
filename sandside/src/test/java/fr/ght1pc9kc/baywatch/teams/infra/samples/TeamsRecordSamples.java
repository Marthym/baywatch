package fr.ght1pc9kc.baywatch.teams.infra.samples;

import fr.ght1pc9kc.baywatch.dsl.tables.Teams;
import fr.ght1pc9kc.baywatch.dsl.tables.records.TeamsRecord;
import fr.ght1pc9kc.baywatch.tests.samples.infra.UsersRecordSamples;
import fr.ght1pc9kc.testy.jooq.model.RelationalDataSet;

import java.time.LocalDateTime;
import java.util.List;

public class TeamsRecordSamples implements RelationalDataSet<TeamsRecord> {
    public static final TeamsRecordSamples SAMPLE = new TeamsRecordSamples();

    public static final TeamsRecord JEDI_TEAM = Teams.TEAMS.newRecord()
            .setTeamId("TM01GP696RFPTY32WD79CVB0KDTF")
            .setTeamName("Jedi Team")
            .setTeamTopic("The light side of the Force")
            .setTeamCreatedAt(LocalDateTime.parse("2023-01-10T22:52:42"))
            .setTeamCreatedBy(UsersRecordSamples.LSKYWALKER.getUserId());

    public static final TeamsRecord SITH_TEAM = Teams.TEAMS.newRecord()
            .setTeamId("TM01GPETWVATJ968SJ717NRHYSEZ")
            .setTeamName("Sith Team")
            .setTeamTopic("The dark side of the Force")
            .setTeamCreatedAt(LocalDateTime.parse("2023-01-10T22:52:42"))
            .setTeamCreatedBy(UsersRecordSamples.DSIDIOUS.getUserId());

    @Override
    public List<TeamsRecord> records() {
        return List.of(JEDI_TEAM, SITH_TEAM);
    }

}
