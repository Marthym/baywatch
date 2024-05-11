package fr.ght1pc9kc.baywatch.tests.samples.infra;

import fr.ght1pc9kc.baywatch.dsl.tables.Users;
import fr.ght1pc9kc.baywatch.dsl.tables.records.UsersRecord;
import fr.ght1pc9kc.baywatch.tests.samples.UserSamples;
import fr.ght1pc9kc.testy.jooq.model.RelationalDataSet;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;

import static fr.ght1pc9kc.baywatch.common.api.DefaultMeta.createdAt;

public class UsersRecordSamples implements RelationalDataSet<UsersRecord> {
    public static final UsersRecordSamples SAMPLE = new UsersRecordSamples();

    public static final UsersRecord OKENOBI = Users.USERS.newRecord()
            .setUserId(UserSamples.OBIWAN.id())
            .setUserCreatedAt(UserSamples.OBIWAN.meta(createdAt, Instant.class)
                    .map(i -> i.atOffset(ZoneOffset.UTC).toLocalDateTime()).orElseThrow())
            .setUserLogin(UserSamples.OBIWAN.self().login())
            .setUserName(UserSamples.OBIWAN.self().name())
            .setUserPassword(UserSamples.OBIWAN.self().password())
            .setUserEmail(UserSamples.OBIWAN.self().mail());

    public static final UsersRecord LSKYWALKER = Users.USERS.newRecord()
            .setUserId(UserSamples.LUKE.id())
            .setUserCreatedAt(UserSamples.LUKE.meta(createdAt, Instant.class)
                    .map(i -> i.atOffset(ZoneOffset.UTC).toLocalDateTime()).orElseThrow())
            .setUserLogin(UserSamples.LUKE.self().login())
            .setUserName(UserSamples.LUKE.self().name())
            .setUserPassword(UserSamples.LUKE.self().password())
            .setUserEmail(UserSamples.LUKE.self().mail());

    public static final UsersRecord DSIDIOUS = Users.USERS.newRecord()
            .setUserId(UserSamples.DSIDIOUS.id())
            .setUserCreatedAt(LocalDateTime.parse("2023-01-10T22:59:42"))
            .setUserLogin("dsidious")
            .setUserName("Dark Sidious")
            .setUserPassword("souidisd")
            .setUserEmail("darth.sidious@sith.com");

    @Override
    public List<UsersRecord> records() {
        return List.of(OKENOBI, LSKYWALKER);
    }
}
