package db.migration;

import com.github.f4b6a3.ulid.UlidFactory;
import lombok.extern.slf4j.Slf4j;
import org.flywaydb.core.api.migration.BaseJavaMigration;
import org.flywaydb.core.api.migration.Context;
import org.jooq.DSLContext;
import org.jooq.impl.DSL;

import java.sql.Connection;
import java.util.List;
import java.util.Map;

import static fr.ght1pc9kc.baywatch.dsl.tables.Configuration.CONFIGURATION;
import static java.util.Objects.nonNull;
import static java.util.Objects.requireNonNull;

@Slf4j
@SuppressWarnings({"unused", "java:S101", "java:S106"})
public class V2_2_202508061136__insert_default_config extends BaseJavaMigration {
    @Override
    public void migrate(Context context) throws Exception {
        Connection conn = context.getConnection();
        var previousAutocommit = conn.getAutoCommit();
        conn.setAutoCommit(false);
        DSLContext dsl = DSL.using(conn);
        UlidFactory ulidFactory = UlidFactory.newMonotonicInstance();

        Map<String, String> env = System.getenv();
        String bwMailSmtpHost = env.get("BW_MAIL_SMTP_HOST");
        if (nonNull(bwMailSmtpHost)) {
            requireNonNull(env.get("BW_MAIL_SMTP_PORT"), "BW_MAIL_SMTP_PORT is required");
            requireNonNull(env.get("BW_MAIL_SMTP_SECURE"), "BW_MAIL_SMTP_SECURE is required");
            requireNonNull(env.get("BW_MAIL_SMTP_USERNAME"), "BW_MAIL_SMTP_USERNAME is required");
            requireNonNull(env.get("BW_MAIL_SMTP_PASSWORD"), "BW_MAIL_SMTP_PASSWORD is required");
            requireNonNull(env.get("BW_MAIL_SMTP_CIPHER"), "BW_MAIL_SMTP_CIPHER is required");
            requireNonNull(env.get("BW_MAIL_SMTP_REQUIRE_TLS"), "BW_MAIL_SMTP_REQUIRE_TLS is required");
        }

        var records = List.of(
                CONFIGURATION.newRecord()
                        .setConfId(ulidFactory.create().toString())
                        .setConfName("mail.smtp.host")
                        .setConfValue(bwMailSmtpHost),
                CONFIGURATION.newRecord()
                        .setConfId(ulidFactory.create().toString())
                        .setConfName("mail.smtp.port")
                        .setConfValue(env.get("BW_MAIL_SMTP_PORT")),
                CONFIGURATION.newRecord()
                        .setConfId(ulidFactory.create().toString())
                        .setConfName("mail.smtp.secure")
                        .setConfValue(env.get("BW_MAIL_SMTP_SECURE")),
                CONFIGURATION.newRecord()
                        .setConfId(ulidFactory.create().toString())
                        .setConfName("mail.smtp.username")
                        .setConfValue(env.get("BW_MAIL_SMTP_USERNAME")),
                CONFIGURATION.newRecord()
                        .setConfId(ulidFactory.create().toString())
                        .setConfName("mail.smtp.password")
                        .setConfValue(env.get("BW_MAIL_SMTP_PASSWORD")),
                CONFIGURATION.newRecord()
                        .setConfId(ulidFactory.create().toString())
                        .setConfName("mail.smtp.cipher")
                        .setConfValue(env.get("BW_MAIL_SMTP_CIPHER")),
                CONFIGURATION.newRecord()
                        .setConfId(ulidFactory.create().toString())
                        .setConfName("mail.smtp.requireTls")
                        .setConfValue(env.get("BW_MAIL_SMTP_REQUIRE_TLS"))
        );

        dsl.batchInsert(records).execute();

        log.atInfo().addArgument(bwMailSmtpHost)
                .log("SMTP server configured with {}");
    }
}
