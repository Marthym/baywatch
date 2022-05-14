package fr.ght1pc9kc.baywatch.scraper.infra.adapters;

import fr.ght1pc9kc.baywatch.scraper.api.FeedScraperPlugin;
import fr.ght1pc9kc.baywatch.scraper.api.NewsEnrichmentService;
import fr.ght1pc9kc.baywatch.scraper.api.RssAtomParser;
import fr.ght1pc9kc.baywatch.scraper.api.ScrapingHandler;
import fr.ght1pc9kc.baywatch.scraper.domain.FeedScraperService;
import fr.ght1pc9kc.baywatch.scraper.domain.model.ScraperConfig;
import fr.ght1pc9kc.baywatch.scraper.infra.config.ScraperProperties;
import fr.ght1pc9kc.baywatch.scraper.infra.config.ScraperQualifier;
import fr.ght1pc9kc.baywatch.techwatch.api.SystemMaintenanceService;
import lombok.SneakyThrows;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.Collection;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@DependsOn({"flyway", "flywayInitializer"}) // Wait after Flyway migrations
public class FeedScraperAdapter {
    private final FeedScraperService scraper;
    private final boolean startScraper;

    public FeedScraperAdapter(SystemMaintenanceService systemMaintenanceService,
                              ScraperProperties properties,
                              RssAtomParser rssAtomParser,
                              Collection<ScrapingHandler> scrappingHandlers,
                              Collection<FeedScraperPlugin> scrapperPlugins,
                              @ScraperQualifier WebClient webClient,
                              NewsEnrichmentService newsEnrichmentService
    ) {
        Map<String, FeedScraperPlugin> plugins = scrapperPlugins.stream()
                .collect(Collectors.toUnmodifiableMap(FeedScraperPlugin::pluginForDomain, Function.identity()));
        this.startScraper = properties.start();
        ScraperConfig config = new ScraperConfig(properties.frequency(), properties.conservation());
        this.scraper = new FeedScraperService(
                config, systemMaintenanceService, webClient,
                rssAtomParser, scrappingHandlers, plugins, newsEnrichmentService);
    }

    @PostConstruct
    void startScrapping() {
        if (startScraper) {
            scraper.startScrapping();
        }
    }

    @PreDestroy
    @SneakyThrows
    void shutdownScrapping() {
        scraper.shutdownScrapping();
    }
}