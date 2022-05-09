package fr.ght1pc9kc.baywatch.scrapper.infra.adapters;

import fr.ght1pc9kc.baywatch.scrapper.api.FeedScrapperPlugin;
import fr.ght1pc9kc.baywatch.scrapper.api.NewsFilter;
import fr.ght1pc9kc.baywatch.scrapper.api.RssAtomParser;
import fr.ght1pc9kc.baywatch.scrapper.api.ScrappingHandler;
import fr.ght1pc9kc.baywatch.scrapper.domain.FeedScrapperService;
import fr.ght1pc9kc.baywatch.scrapper.infra.config.ScraperQualifier;
import fr.ght1pc9kc.baywatch.scrapper.infra.config.ScraperProperties;
import fr.ght1pc9kc.baywatch.techwatch.api.FeedAdminService;
import fr.ght1pc9kc.baywatch.techwatch.domain.ports.FeedPersistencePort;
import fr.ght1pc9kc.baywatch.techwatch.domain.ports.NewsPersistencePort;
import lombok.SneakyThrows;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@DependsOn({"flyway", "flywayInitializer"}) // Wait after Flyway migrations
public class FeedScrapperAdapter {
    private final FeedScrapperService scrapper;
    private final boolean startScrapper;

    public FeedScrapperAdapter(FeedAdminService feedAdminService, NewsPersistencePort newsPersistence,
                               ScraperProperties properties,
                               RssAtomParser rssAtomParser,
                               Collection<ScrappingHandler> scrappingHandlers,
                               Collection<FeedScrapperPlugin> scrapperPlugins,
                               @ScraperQualifier WebClient webClient,
                               List<NewsFilter> newsFilters
    ) {
        Map<String, FeedScrapperPlugin> plugins = scrapperPlugins.stream()
                .collect(Collectors.toUnmodifiableMap(FeedScrapperPlugin::pluginForDomain, Function.identity()));
        this.startScrapper = properties.start();
        this.scrapper = new FeedScrapperService(
                properties, feedAdminService, newsPersistence, webClient,
                rssAtomParser, scrappingHandlers, plugins, newsFilters);
    }

    @PostConstruct
    void startScrapping() {
        if (startScrapper) {
            scrapper.startScrapping();
        }
    }

    @PreDestroy
    @SneakyThrows
    void shutdownScrapping() {
        scrapper.shutdownScrapping();
    }
}
