package fr.ght1pc9kc.baywatch.tests.samples.infra;

import fr.ght1pc9kc.baywatch.dsl.tables.records.UsersRolesRecord;
import fr.ght1pc9kc.baywatch.tests.samples.UserSamples;
import fr.ght1pc9kc.testy.jooq.model.RelationalDataSet;

import java.util.ArrayList;
import java.util.List;

import static fr.ght1pc9kc.baywatch.dsl.tables.UsersRoles.USERS_ROLES;

public class UsersRolesSamples implements RelationalDataSet<UsersRolesRecord> {
    public static final RelationalDataSet<UsersRolesRecord> SAMPLE = new UsersRolesSamples();

    @Override
    public List<UsersRolesRecord> records() {
        List<UsersRolesRecord> records = new ArrayList<>(
                UserSamples.OBIWAN.self.roles.size() + UserSamples.LUKE.self.roles.size());
        records.addAll(UserSamples.OBIWAN.self.roles.stream()
                .map(r -> USERS_ROLES.newRecord()
                        .setUsroUserId(UserSamples.OBIWAN.id)
                        .setUsroRole(r.toString()))
                .toList());
        records.addAll(UserSamples.LUKE.self.roles.stream()
                .map(r -> USERS_ROLES.newRecord()
                        .setUsroUserId(UserSamples.LUKE.id)
                        .setUsroRole(r.toString()))
                .toList());
        return List.copyOf(records);
    }
}
