package db.migration;

import com.github.f4b6a3.ulid.Ulid;
import fr.ght1pc9kc.baywatch.dsl.tables.Teams;
import fr.ght1pc9kc.baywatch.dsl.tables.records.UsersRecord;
import lombok.extern.slf4j.Slf4j;
import org.flywaydb.core.api.migration.BaseJavaMigration;
import org.flywaydb.core.api.migration.Context;
import org.jooq.Cursor;
import org.jooq.DSLContext;
import org.jooq.impl.DSL;

import java.sql.Connection;
import java.util.concurrent.atomic.AtomicReference;

import static fr.ght1pc9kc.baywatch.dsl.tables.FeedsUsers.FEEDS_USERS;
import static fr.ght1pc9kc.baywatch.dsl.tables.NewsUserState.NEWS_USER_STATE;
import static fr.ght1pc9kc.baywatch.dsl.tables.Teams.TEAMS;
import static fr.ght1pc9kc.baywatch.dsl.tables.TeamsMembers.TEAMS_MEMBERS;
import static fr.ght1pc9kc.baywatch.dsl.tables.Users.USERS;
import static fr.ght1pc9kc.baywatch.dsl.tables.UsersRoles.USERS_ROLES;

@Slf4j
public class V2_0_4__migration_user_to_ULID extends BaseJavaMigration {
    @Override
    public void migrate(Context context) throws Exception {
        log.info("Start migration V2_0_4__migration_user_to_ULID");
        var ulid = new AtomicReference<>(Ulid.fast());
        try {
            Connection conn = context.getConnection();
            var previousAutocommit = conn.getAutoCommit();
            conn.setAutoCommit(false);
            DSLContext dsl = DSL.using(conn);
            try (Cursor<UsersRecord> cursor = dsl.selectFrom(USERS).fetchLazy()) {
                while (cursor.hasNext()) {
                    UsersRecord usersRecord = cursor.fetchNext();
                    String oldUserId = usersRecord.getUserId();
                    String newUserId = "US" + ulid.getAndUpdate(Ulid::increment).toString();
                    log.info("migrate user from {} to {}", oldUserId, newUserId);
                    dsl.update(USERS).set(USERS.USER_ID, newUserId)
                            .where(USERS.USER_ID.eq(oldUserId))
                            .execute();
                    dsl.update(USERS_ROLES).set(USERS_ROLES.USRO_USER_ID, newUserId)
                            .where(USERS_ROLES.USRO_USER_ID.eq(oldUserId))
                            .execute();
                    dsl.update(FEEDS_USERS).set(FEEDS_USERS.FEUS_USER_ID, newUserId)
                            .where(FEEDS_USERS.FEUS_USER_ID.eq(oldUserId))
                            .execute();
                    dsl.update(NEWS_USER_STATE).set(NEWS_USER_STATE.NURS_USER_ID, newUserId)
                            .where(NEWS_USER_STATE.NURS_USER_ID.eq(oldUserId))
                            .execute();
                    dsl.update(TEAMS)
                            .set(TEAMS.TEAM_CREATED_BY, newUserId)
                            .where(TEAMS.TEAM_CREATED_BY.eq(oldUserId))
                            .execute();
                    dsl.update(TEAMS_MEMBERS)
                            .set(TEAMS_MEMBERS.TEME_USER_ID, newUserId)
                            .where(TEAMS_MEMBERS.TEME_USER_ID.eq(oldUserId))
                            .execute();
                    dsl.update(TEAMS_MEMBERS)
                            .set(TEAMS_MEMBERS.TEME_CREATED_BY, newUserId)
                            .where(TEAMS_MEMBERS.TEME_CREATED_BY.eq(oldUserId))
                            .execute();
                    log.info("user {} migrated successfully !", newUserId);
                }
            }
            conn.setAutoCommit(previousAutocommit);
            conn.commit();
            log.info("Transaction committed !");
        } catch (Exception e) {
            log.error("STACKTRACE", e);
        }
    }
}
