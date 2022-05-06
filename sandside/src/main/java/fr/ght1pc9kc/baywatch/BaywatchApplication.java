package fr.ght1pc9kc.baywatch;

import fr.ght1pc9kc.baywatch.scrapper.infra.config.ScraperProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

@Slf4j
@SpringBootApplication
@EnableConfigurationProperties({ScraperProperties.class})
public class BaywatchApplication {

    public static final String APPLICATION_NAME = "Baywatch";

    public static void main(String[] args) {
        SpringApplication.run(BaywatchApplication.class, args);
    }

    @Bean
    Scheduler getDatabaseScheduler() {
        return Schedulers.newBoundedElastic(5, Integer.MAX_VALUE, "database");
    }
}
