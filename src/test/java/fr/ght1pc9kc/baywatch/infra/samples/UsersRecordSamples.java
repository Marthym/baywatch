package fr.ght1pc9kc.baywatch.infra.samples;

import fr.ght1pc9kc.baywatch.domain.utils.Hasher;
import fr.ght1pc9kc.baywatch.dsl.tables.Users;
import fr.ght1pc9kc.baywatch.dsl.tables.records.UsersRecord;
import fr.irun.testy.jooq.model.RelationalDataSet;

import java.util.List;

public class UsersRecordSamples implements RelationalDataSet<UsersRecord> {
    public static final UsersRecordSamples SAMPLE = new UsersRecordSamples();

    public static final UsersRecord OKENOBI = Users.USERS.newRecord()
            .setUserId(Hasher.sha3("Obiwan Kenobi"))
            .setUserLogin("okenobi")
            .setUserName("Obiwan Kenobi")
            .setUserEmail("obiwan.kenobi@jedi.fr");

    public static final UsersRecord LSKYWALKER = Users.USERS.newRecord()
            .setUserId(Hasher.sha3("Luke Skywalker"))
            .setUserLogin("lskywalker")
            .setUserName("Luke Skywalker")
            .setUserEmail("luke.skywalker@jedi.fr");

    @Override
    public List<UsersRecord> records() {
        return List.of(OKENOBI, LSKYWALKER);
    }
}
