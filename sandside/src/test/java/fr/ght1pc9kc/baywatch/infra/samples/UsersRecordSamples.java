package fr.ght1pc9kc.baywatch.infra.samples;

import fr.ght1pc9kc.baywatch.api.security.model.Role;
import fr.ght1pc9kc.baywatch.domain.utils.Hasher;
import fr.ght1pc9kc.baywatch.dsl.tables.Users;
import fr.ght1pc9kc.baywatch.dsl.tables.records.UsersRecord;
import fr.irun.testy.jooq.model.RelationalDataSet;

import java.net.URI;
import java.util.List;

public class UsersRecordSamples implements RelationalDataSet<UsersRecord> {
    public static final UsersRecordSamples SAMPLE = new UsersRecordSamples();

    public static final UsersRecord OKENOBI = Users.USERS.newRecord()
            .setUserId(Hasher.identify(URI.create("https://obiwan.kenobi.jedi")))
            .setUserLogin("okenobi")
            .setUserName("Obiwan Kenobi")
            .setUserPassword("$2a$10$gPCnlSwTrPwXDBe3NFY68urYQL3sUgsXBVTiSwR7Ev/wT8nDHq7HO") // obiwan
            .setUserEmail("obiwan.kenobi@jedi.fr")
            .setUserRole(Role.MANAGER.name());

    public static final UsersRecord LSKYWALKER = Users.USERS.newRecord()
            .setUserId(Hasher.identify(URI.create("https://luke.skywalker.jedi")))
            .setUserLogin("lskywalker")
            .setUserName("Luke Skywalker")
            .setUserPassword("$2a$10$axBjnH2bQGIm8bpKnFcIR.apDNLp68ncvnw2AyTRTeRmI8qg2zkj2") // luke
            .setUserEmail("luke.skywalker@jedi.fr")
            .setUserRole(Role.USER.name());

    @Override
    public List<UsersRecord> records() {
        return List.of(OKENOBI, LSKYWALKER);
    }
}
