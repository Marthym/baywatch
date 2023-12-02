package fr.ght1pc9kc.baywatch.scraper.infra.config;

import fr.ght1pc9kc.scraphead.core.HeadScraper;
import fr.ght1pc9kc.scraphead.core.HeadScrapers;
import fr.ght1pc9kc.scraphead.core.http.ScrapClient;
import fr.ght1pc9kc.scraphead.netty.http.NettyScrapClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.netty.http.client.HttpClient;

@Configuration
public class OpenGraphConfig {
    @Bean
    public HeadScraper getOpenGraphScrapper(HttpClient httpClient) {
        ScrapClient scrapClient = new NettyScrapClient(httpClient);
        return HeadScrapers.builder(scrapClient).build();
    }
}
