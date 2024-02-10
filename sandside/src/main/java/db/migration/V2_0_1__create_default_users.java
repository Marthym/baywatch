package db.migration;

import com.github.f4b6a3.ulid.Ulid;
import fr.ght1pc9kc.baywatch.dsl.tables.records.UsersRecord;
import fr.ght1pc9kc.baywatch.dsl.tables.records.UsersRolesRecord;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.flywaydb.core.api.migration.BaseJavaMigration;
import org.flywaydb.core.api.migration.Context;
import org.jooq.DSLContext;
import org.jooq.impl.DSL;
import org.springframework.security.crypto.bcrypt.BCrypt;

import java.sql.Connection;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Stream;

import static fr.ght1pc9kc.baywatch.dsl.tables.Users.USERS;
import static fr.ght1pc9kc.baywatch.dsl.tables.UsersRoles.USERS_ROLES;

@Slf4j
@SuppressWarnings({"unused", "java:S101", "java:S106"})
public class V2_0_1__create_default_users extends BaseJavaMigration {
    @Override
    public void migrate(Context context) throws Exception {
        try {
            Connection conn = context.getConnection();
            var previousAutocommit = conn.getAutoCommit();
            conn.setAutoCommit(false);
            DSLContext dsl = DSL.using(conn);

            int userCount = dsl.fetchCount(USERS);
            if (userCount > 0) {
                return;
            }

            System.out.println("## -----------------------------------------------");
            System.out.println("## ");
            AtomicReference<Ulid> idGenerator = new AtomicReference<>(Ulid.fast());

            List<UsersRecord> usersRecords = new ArrayList<>();
            List<UsersRolesRecord> rolesRecords = new ArrayList<>();
            Stream.of("admin", "actuator").forEach(user -> {
                String password = RandomStringUtils.randomPrint(15, 20);
                String bcryptedPassword = BCrypt.hashpw(password, BCrypt.gensalt());

                String userId = "US" + idGenerator.getAndUpdate(Ulid::increment);
                usersRecords.add(USERS.newRecord()
                        .setUserId(userId)
                        .setUserLogin(user)
                        .setUserEmail(user + "@localhost.internal")
                        .setUserName("Baywatch " + StringUtils.capitalize(user))
                        .setUserPassword(bcryptedPassword)
                        .setUserCreatedAt(LocalDateTime.now()));

                rolesRecords.add(USERS_ROLES.newRecord()
                        .setUsroUserId(userId)
                        .setUsroRole(user.toUpperCase()));

                System.out.printf("## Password for %-9s: %s%n", user, password);
            });

            dsl.batchInsert(usersRecords).execute();
            dsl.batchInsert(rolesRecords).execute();

            conn.commit();
            conn.setAutoCommit(previousAutocommit);

            System.out.println("## ");
            System.out.println("## -----------------------------------------------");
        } catch (Exception e) {
            log.error("STACKTRACE", e);
        }
    }
}
