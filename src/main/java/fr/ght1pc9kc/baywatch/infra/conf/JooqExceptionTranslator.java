package fr.ght1pc9kc.baywatch.infra.conf;

import lombok.extern.slf4j.Slf4j;
import org.jooq.ExecuteContext;
import org.jooq.SQLDialect;
import org.jooq.impl.DefaultExecuteListener;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.support.SQLErrorCodeSQLExceptionTranslator;
import org.springframework.jdbc.support.SQLExceptionTranslator;
import org.springframework.jdbc.support.SQLStateSQLExceptionTranslator;

import java.sql.SQLException;

@Slf4j
public class JooqExceptionTranslator extends DefaultExecuteListener {
    @Override
    public void exception(ExecuteContext context) {
        SQLExceptionTranslator translator = getTranslator(context);
        // The exception() callback is not only triggered for SQL exceptions but also for
        // "normal" exceptions. In those cases sqlException() returns null.
        SQLException exception = context.sqlException();
        while (exception != null) {
            handle(context, translator, exception);
            exception = exception.getNextException();
        }
    }

    private SQLExceptionTranslator getTranslator(ExecuteContext context) {
        SQLDialect dialect = context.configuration().dialect();
        String dbName = dialect.getName();
        return new SQLErrorCodeSQLExceptionTranslator(dbName);
    }

    /**
     * Handle a single exception in the chain. SQLExceptions might be nested multiple
     * levels deep. The outermost exception is usually the least interesting one ("Call
     * getNextException to see the cause."). Therefore the innermost exception is
     * propagated and all other exceptions are logged.
     *
     * @param context    the execute context
     * @param translator the exception translator
     * @param exception  the exception
     */
    private void handle(ExecuteContext context, SQLExceptionTranslator translator, SQLException exception) {
        DataAccessException translated = translate(context, translator, exception);
        if (exception.getNextException() == null) {
            context.exception(translated);
        } else {
            log.error("Execution of SQL statement failed.", translated);
        }
    }

    private DataAccessException translate(ExecuteContext context, SQLExceptionTranslator translator,
                                          SQLException exception) {
        return translator.translate("jOOQ", context.sql(), exception);
    }
}
