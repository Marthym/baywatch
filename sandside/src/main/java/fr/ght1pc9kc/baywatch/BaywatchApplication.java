package fr.ght1pc9kc.baywatch;

import fr.ght1pc9kc.baywatch.domain.scrapper.opengraph.OpenGraphMetaReader;
import fr.ght1pc9kc.baywatch.domain.scrapper.opengraph.OpenGraphPlugin;
import fr.ght1pc9kc.baywatch.domain.scrapper.opengraph.OpenGraphScrapper;
import fr.ght1pc9kc.baywatch.infra.config.ScrapperProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;
import reactor.netty.http.client.HttpClient;

import java.util.List;

@Slf4j
@SpringBootApplication
@EnableConfigurationProperties({ScrapperProperties.class})
public class BaywatchApplication {

    public static final String APPLICATION_NAME = "Baywatch";

    public static void main(String[] args) {
        SpringApplication.run(BaywatchApplication.class, args);
    }

    @Bean
    Scheduler getDatabaseScheduler() {
        return Schedulers.newBoundedElastic(5, Integer.MAX_VALUE, "database");
    }

    @Bean
    WebClient getWebClient() {
        return WebClient.builder()
                .clientConnector(new ReactorClientHttpConnector(
                        HttpClient.create()
                                .followRedirect(true)
                                .compress(true)
                )).build();
    }

    @Bean
    OpenGraphScrapper getOpenGraphScrapper(WebClient webClient, List<OpenGraphPlugin> plugins) {
        return new OpenGraphScrapper(webClient, new OpenGraphMetaReader(), plugins);
    }
}
