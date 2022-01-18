package fr.ght1pc9kc.baywatch.infra.scrapper;

import fr.ght1pc9kc.baywatch.domain.scrapper.opengraph.OpenGraphMetaReader;
import fr.ght1pc9kc.baywatch.domain.scrapper.opengraph.OpenGraphPlugin;
import fr.ght1pc9kc.baywatch.domain.scrapper.opengraph.OpenGraphScrapper;
import fr.ght1pc9kc.baywatch.domain.scrapper.opengraph.plugins.ValidateOGLinksPlugin;
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
    OpenGraphScrapper getOpenGraphScrapper(WebClient webClient, List<OpenGraphPlugin> plugins) {
        return new OpenGraphScrapper(webClient, new OpenGraphMetaReader(), plugins);
    }
}