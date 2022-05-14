package fr.ght1pc9kc.baywatch.scraper.infra.config;

import fr.ght1pc9kc.scraphead.core.HeadScraper;
import fr.ght1pc9kc.scraphead.core.HeadScrapers;
import fr.ght1pc9kc.scraphead.core.http.ScrapClient;
import fr.ght1pc9kc.scraphead.netty.http.NettyScrapClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenGraphConfig {

    @Bean
    HeadScraper getOpenGraphScrapper() {
        ScrapClient scrapClient = new NettyScrapClient();
        return HeadScrapers.builder(scrapClient).build();
    }
}
