package fr.ght1pc9kc.baywatch.scrapper.infra;

import fr.ght1pc9kc.baywatch.scrapper.domain.opengraph.plugins.ValidateOGLinksPlugin;
import fr.ght1pc9kc.scraphead.core.HeadScrapers;
import fr.ght1pc9kc.scraphead.core.HeadScrapper;
import fr.ght1pc9kc.scraphead.core.OpenGraphPlugin;
import fr.ght1pc9kc.scraphead.netty.http.NettyWebClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;

@Configuration
public class OpenGraphConfig {
    @Bean
    ValidateOGLinksPlugin getValidateImageUriPlugin(WebClient webClient) {
        return new ValidateOGLinksPlugin(webClient);
    }

    @Bean
    HeadScrapper getOpenGraphScrapper(List<OpenGraphPlugin> plugins) {
        fr.ght1pc9kc.scraphead.core.http.WebClient webClient = new NettyWebClient();
        HeadScrapers.HeadScraperBuilder builder = HeadScrapers.builder(webClient);
        plugins.forEach(builder::registerPlugin);
        return builder.build();
    }
}
