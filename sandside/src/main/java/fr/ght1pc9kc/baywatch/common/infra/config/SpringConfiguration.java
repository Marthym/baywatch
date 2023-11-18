package fr.ght1pc9kc.baywatch.common.infra.config;

import fr.ght1pc9kc.baywatch.common.infra.DatabaseQualifier;
import fr.ght1pc9kc.juery.api.filter.CriteriaVisitor;
import fr.ght1pc9kc.juery.basic.filter.ListPropertiesCriteriaVisitor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

import java.util.List;

@Configuration
public class SpringConfiguration {
    @Bean
    public CriteriaVisitor<List<String>> getListPropertiesVisitor() {
        return new ListPropertiesCriteriaVisitor();
    }

    @Bean
    @DatabaseQualifier
    Scheduler getDatabaseScheduler() {
        return Schedulers.newBoundedElastic(5, Integer.MAX_VALUE, "database");
    }
}
