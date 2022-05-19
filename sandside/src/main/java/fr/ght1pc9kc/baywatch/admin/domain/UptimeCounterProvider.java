package fr.ght1pc9kc.baywatch.admin.domain;

import fr.ght1pc9kc.baywatch.admin.api.model.Counter;
import fr.ght1pc9kc.baywatch.admin.api.model.CounterGroup;
import fr.ght1pc9kc.baywatch.admin.api.model.CounterProvider;
import fr.ght1pc9kc.baywatch.common.api.HeroIcons;
import reactor.core.publisher.Mono;

import java.lang.management.ManagementFactory;
import java.time.Duration;

public class UptimeCounterProvider implements CounterProvider {
    @Override
    public CounterGroup group() {
        return CounterGroup.SYSTEM;
    }

    @Override
    public Mono<Counter> computeCounter() {
        var rb = ManagementFactory.getRuntimeMXBean();
        Duration d = Duration.ofMillis(rb.getUptime());
        String value = (d.toDaysPart() == 0)
                ? String.format("%dh %02dm %02ds", d.toHoursPart(), d.toMinutesPart(), d.toSecondsPart())
                : String.format("%dd %dh %02dm", d.toDaysPart(), d.toHoursPart(), d.toMinutesPart());
        String description = String.format("%s %s", rb.getVmVendor(), rb.getVmVersion());
        return Mono.just(Counter.create("Uptime", HeroIcons.ClockIcon, value, description));
    }
}
