package fr.ght1pc9kc.baywatch;

import fr.ght1pc9kc.baywatch.infra.conf.JooqExceptionTranslator;
import org.jooq.impl.DefaultExecuteListener;
import org.jooq.impl.DefaultExecuteListenerProvider;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.core.annotation.Order;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

import java.time.Clock;

@SpringBootApplication
public class BaywatchApplication {

    public static void main(String[] args) {
        SpringApplication.run(BaywatchApplication.class, args);
    }

    @Bean
    Clock getSystemUTCClock() {
        return Clock.systemUTC();
    }

    @Bean
    Scheduler getDatabaseScheduler() {
        return Schedulers.newBoundedElastic(5, Integer.MAX_VALUE, "sqlite");
    }

//    @Bean
//    @Order(0)
//    public DefaultExecuteListenerProvider jooqExceptionTranslatorBaywatch() {
//        return new DefaultExecuteListenerProvider(new DefaultExecuteListener());
//    }
}
