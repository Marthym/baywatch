package fr.ght1pc9kc.baywatch.scraper.infra.adapters;

import fr.ght1pc9kc.baywatch.common.api.EventHandler;
import fr.ght1pc9kc.baywatch.scraper.api.FeedScraperPlugin;
import fr.ght1pc9kc.baywatch.scraper.api.FeedScraperService;
import fr.ght1pc9kc.baywatch.scraper.api.RssAtomParser;
import fr.ght1pc9kc.baywatch.scraper.api.ScrapEnrichmentService;
import fr.ght1pc9kc.baywatch.scraper.api.model.AtomFeed;
import fr.ght1pc9kc.baywatch.scraper.domain.FeedScraperServiceImpl;
import fr.ght1pc9kc.baywatch.scraper.domain.ports.NewsMaintenancePort;
import fr.ght1pc9kc.baywatch.scraper.infra.config.ScraperApplicationProperties;
import fr.ght1pc9kc.baywatch.scraper.infra.config.ScraperQualifier;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.SneakyThrows;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;

import java.net.URI;
import java.util.Collection;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@DependsOn({"flyway", "flywayInitializer"}) // Wait after Flyway migrations
public class FeedScraperAdapter implements FeedScraperService {
    private final FeedScraperServiceImpl scraper;
    private final boolean startScraper;

    public FeedScraperAdapter(NewsMaintenancePort newsMaintenancePort,
                              ScraperApplicationProperties properties,
                              @ScraperQualifier Scheduler scraperScheduler,
                              RssAtomParser rssAtomParser,
                              Collection<EventHandler> scrappingHandlers,
                              Collection<FeedScraperPlugin> scrapperPlugins,
                              @ScraperQualifier WebClient webClient,
                              ScrapEnrichmentService scrapEnrichmentService
    ) {
        Map<String, FeedScraperPlugin> plugins = scrapperPlugins.stream()
                .collect(Collectors.toUnmodifiableMap(FeedScraperPlugin::pluginForDomain, Function.identity()));
        this.startScraper = properties.enable();
        this.scraper = new FeedScraperServiceImpl(
                properties, scraperScheduler, newsMaintenancePort, webClient,
                rssAtomParser, scrappingHandlers, plugins, scrapEnrichmentService);
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

    @Override
    public Mono<AtomFeed> scrapFeedHeader(URI link) {
        return scraper.scrapFeedHeader(link);
    }
}
