package fr.ght1pc9kc.baywatch.common.infra.adapters;

import fr.ght1pc9kc.baywatch.common.api.model.BaywatchLogsMarkers;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.jooq.ExecuteContext;
import org.jooq.ExecuteListener;
import org.jooq.ExecuteListenerProvider;
import org.jooq.conf.ParamType;
import org.jooq.tools.StopWatch;

import java.time.Duration;
import java.util.Optional;

@Slf4j
public class PerformanceJooqListener implements ExecuteListener {
    private static final long SLOW_QUERY_THRESHOLD = 400;
    private transient StopWatch watch;

    @Override
    public void executeStart(ExecuteContext ctx) {
        ExecuteListener.super.executeStart(ctx);
        watch = new StopWatch();
    }

    @Override
    public void executeEnd(ExecuteContext ctx) {
        ExecuteListener.super.executeEnd(ctx);
        Duration elapsed = Duration.ofNanos(watch.split());
        if (elapsed.toMillis() > SLOW_QUERY_THRESHOLD) {
            log.atWarn()
                    .addArgument(elapsed.toMillis())
                    .addArgument(() -> Optional.ofNullable(ctx.query())
                            .map(q -> q.getSQL(ParamType.INLINED))
                            .orElse("UNKNOWN"))
                    .addMarker(BaywatchLogsMarkers.PERFORMANCE)
                    .log("Slow query executed in {}ms : {}");
        } else if (log.isTraceEnabled()) {
            log.atTrace()
                    .addArgument(elapsed.toMillis())
                    .addArgument(() -> Optional.ofNullable(ctx.query())
                            .map(q -> q.getSQL(ParamType.INLINED))
                            .orElse("UNKNOWN"))
                    .addMarker(BaywatchLogsMarkers.PERFORMANCE)
                    .log("Query executed in {}ms : {}");
        }
    }

    public static ExecuteListenerProvider provider() {
        return new ExecuteListenerProvider() {
            @Override
            public @NotNull ExecuteListener provide() {
                return new PerformanceJooqListener();
            }
        };
    }
}
