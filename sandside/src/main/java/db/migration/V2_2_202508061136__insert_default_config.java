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
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static java.util.Objects.requireNonNull;

@Slf4j
@SuppressWarnings({"unused", "java:S101", "java:S106"})
public class V2_2_202508061136__insert_default_config extends BaseJavaMigration {
    private static final String CONFIGURATION_PREFIX = "CF";

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
            requireNonNull(env.get("BW_MAIL_SMTP_USERNAME"), "BW_MAIL_SMTP_USERNAME is required");
            requireNonNull(env.get("BW_MAIL_SMTP_PASSWORD"), "BW_MAIL_SMTP_PASSWORD is required");
            requireNonNull(env.get("BW_MAIL_SMTP_FROM"), "BW_MAIL_SMTP_FROM is required");
        }

        if (isNull(bwMailSmtpHost)) {
            log.atWarn().log("No SMTP server configured, skipping mail configuration !");
            return;
        }
        var records = List.of(
                CONFIGURATION.newRecord()
                        .setConfId(CONFIGURATION_PREFIX + ulidFactory.create().toString())
                        .setConfName("mail.smtp.host")
                        .setConfValue(bwMailSmtpHost),
                CONFIGURATION.newRecord()
                        .setConfId(CONFIGURATION_PREFIX + ulidFactory.create().toString())
                        .setConfName("mail.smtp.port")
                        .setConfValue(env.getOrDefault("BW_MAIL_SMTP_PORT", "587")),
                CONFIGURATION.newRecord()
                        .setConfId(CONFIGURATION_PREFIX + ulidFactory.create().toString())
                        .setConfName("mail.smtp.secure")
                        .setConfValue(env.getOrDefault("BW_MAIL_SMTP_SECURE", "true")),
                CONFIGURATION.newRecord()
                        .setConfId(CONFIGURATION_PREFIX + ulidFactory.create().toString())
                        .setConfName("mail.smtp.username")
                        .setConfValue(env.get("BW_MAIL_SMTP_USERNAME")),
                CONFIGURATION.newRecord()
                        .setConfId(CONFIGURATION_PREFIX + ulidFactory.create().toString())
                        .setConfName("mail.smtp.password")
                        .setConfValue(env.get("BW_MAIL_SMTP_PASSWORD")),
                CONFIGURATION.newRecord()
                        .setConfId(CONFIGURATION_PREFIX + ulidFactory.create().toString())
                        .setConfName("mail.smtp.ssl.protocols")
                        .setConfValue(env.getOrDefault("BW_MAIL_SMTP_SSL_PROTOCOLS", "TLSv1.3 TLSv1.2")),
                CONFIGURATION.newRecord()
                        .setConfId(CONFIGURATION_PREFIX + ulidFactory.create().toString())
                        .setConfName("mail.smtp.requireTls")
                        .setConfValue(env.getOrDefault("BW_MAIL_SMTP_REQUIRE_TLS", "true")),
                CONFIGURATION.newRecord()
                        .setConfId(CONFIGURATION_PREFIX + ulidFactory.create().toString())
                        .setConfName("mail.smtp.ssl.checkserveridentity")
                        .setConfValue(env.getOrDefault("BW_MAIL_SMTP_CHECK_SERVER_IDENTITY", "true")),
                CONFIGURATION.newRecord()
                        .setConfId(CONFIGURATION_PREFIX + ulidFactory.create().toString())
                        .setConfName("mail.smtp.from")
                        .setConfValue(env.get("BW_MAIL_SMTP_FROM")),
                CONFIGURATION.newRecord()
                        .setConfId(CONFIGURATION_PREFIX + ulidFactory.create().toString())
                        .setConfName("mail.smtp.pollingIntervalSeconds")
                        .setConfValue(env.getOrDefault("BW_MAIL_SMTP_POLLING_INTERVAL_SECONDS", "60"))
        );

        dsl.batchInsert(records).execute();

        log.atInfo().addArgument(bwMailSmtpHost)
                .log("SMTP server configured with {}");
    }
}
