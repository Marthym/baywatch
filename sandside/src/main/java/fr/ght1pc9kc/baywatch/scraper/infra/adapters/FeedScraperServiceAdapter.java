package fr.ght1pc9kc.baywatch.scraper.infra.adapters;

import fr.ght1pc9kc.baywatch.scraper.api.FeedScraperPlugin;
import fr.ght1pc9kc.baywatch.scraper.api.FeedScraperService;
import fr.ght1pc9kc.baywatch.scraper.api.RssAtomParser;
import fr.ght1pc9kc.baywatch.scraper.api.ScrapEnrichmentService;
import fr.ght1pc9kc.baywatch.scraper.domain.FeedScraperServiceImpl;
import fr.ght1pc9kc.baywatch.scraper.api.ScrapingEventHandler;
import fr.ght1pc9kc.baywatch.scraper.domain.ports.ScraperMaintenancePort;
import fr.ght1pc9kc.baywatch.scraper.infra.config.ScraperQualifier;
import lombok.experimental.Delegate;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.scheduler.Scheduler;

import java.util.Collection;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@DependsOn({"flyway", "flywayInitializer"})
public class FeedScraperServiceAdapter implements FeedScraperService {
    @Delegate
    private final FeedScraperService delegate;

    public FeedScraperServiceAdapter(ScraperMaintenancePort scraperMaintenancePort,
                                     @ScraperQualifier Scheduler scraperScheduler,
                                     RssAtomParser rssAtomParser,
                                     Collection<ScrapingEventHandler> scrappingHandlers,
                                     Collection<FeedScraperPlugin> scrapperPlugins,
                                     @ScraperQualifier WebClient webClient,
                                     ScrapEnrichmentService scrapEnrichmentService
    ) {
        Map<String, FeedScraperPlugin> plugins = scrapperPlugins.stream()
                .collect(Collectors.toUnmodifiableMap(FeedScraperPlugin::pluginForDomain, Function.identity()));
        this.delegate = new FeedScraperServiceImpl(
                scraperScheduler, scraperMaintenancePort, webClient,
                rssAtomParser, scrappingHandlers, plugins, scrapEnrichmentService);
    }
}
