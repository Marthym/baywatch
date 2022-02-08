package fr.ght1pc9kc.baywatch.infra.samples;

import fr.ght1pc9kc.baywatch.domain.security.samples.UserSamples;
import fr.ght1pc9kc.baywatch.dsl.tables.Users;
import fr.ght1pc9kc.baywatch.dsl.tables.records.UsersRecord;
import fr.irun.testy.jooq.model.RelationalDataSet;

import java.time.ZoneOffset;
import java.util.List;

public class UsersRecordSamples implements RelationalDataSet<UsersRecord> {
    public static final UsersRecordSamples SAMPLE = new UsersRecordSamples();

    public static final UsersRecord OKENOBI = Users.USERS.newRecord()
            .setUserId(UserSamples.OBIWAN.id)
            .setUserCreatedAt(UserSamples.OBIWAN.createdAt.atOffset(ZoneOffset.UTC).toLocalDateTime())
            .setUserLogin(UserSamples.OBIWAN.entity.login)
            .setUserName(UserSamples.OBIWAN.entity.name)
            .setUserPassword(UserSamples.OBIWAN.entity.password)
            .setUserEmail(UserSamples.OBIWAN.entity.mail)
            .setUserRole(UserSamples.OBIWAN.entity.role.name());

    public static final UsersRecord LSKYWALKER = Users.USERS.newRecord()
            .setUserId(UserSamples.LUKE.id)
            .setUserCreatedAt(UserSamples.LUKE.createdAt.atOffset(ZoneOffset.UTC).toLocalDateTime())
            .setUserLogin(UserSamples.LUKE.entity.login)
            .setUserName(UserSamples.LUKE.entity.name)
            .setUserPassword(UserSamples.LUKE.entity.password)
            .setUserEmail(UserSamples.LUKE.entity.mail)
            .setUserRole(UserSamples.LUKE.entity.role.name());

    @Override
    public List<UsersRecord> records() {
        return List.of(OKENOBI, LSKYWALKER);
    }
}
