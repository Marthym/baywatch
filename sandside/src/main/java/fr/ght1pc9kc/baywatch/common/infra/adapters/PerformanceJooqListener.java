package fr.ght1pc9kc.baywatch.common.infra.adapters;

import fr.ght1pc9kc.baywatch.common.api.model.BaywatchLogsMakers;
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
        if (elapsed.toMillis() > 400) {
            log.atWarn()
                    .addArgument(elapsed)
                    .addArgument(() -> Optional.ofNullable(ctx.query())
                            .map(q -> q.getSQL(ParamType.INLINED))
                            .orElse("UNKNOWN"))
                    .addMarker(BaywatchLogsMakers.PERFORMANCE)
                    .log("jOOQ Meta executed a slow query in {} \n\n {}");
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
