package fr.ght1pc9kc.baywatch.scrapper.infra.adapters;

import fr.ght1pc9kc.baywatch.scrapper.api.FeedScrapperPlugin;
import fr.ght1pc9kc.baywatch.scrapper.api.RssAtomParser;
import fr.ght1pc9kc.baywatch.scrapper.api.ScrappingHandler;
import fr.ght1pc9kc.baywatch.scrapper.domain.FeedScrapperService;
import fr.ght1pc9kc.baywatch.scrapper.infra.config.ScrapperProperties;
import fr.ght1pc9kc.baywatch.security.api.AuthenticationFacade;
import fr.ght1pc9kc.baywatch.techwatch.domain.ports.FeedPersistencePort;
import fr.ght1pc9kc.baywatch.techwatch.domain.ports.NewsPersistencePort;
import fr.ght1pc9kc.scraphead.core.HeadScraper;
import lombok.SneakyThrows;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.Collection;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@DependsOn({"flyway", "flywayInitializer"}) // Wait after Flyway migrations
public class FeedScrapperAdapter {
    private final FeedScrapperService scrapper;
    private final boolean startScrapper;

    public FeedScrapperAdapter(FeedPersistencePort feedPersistence, NewsPersistencePort newsPersistence,
                               HeadScraper ogScrapper, RssAtomParser rssAtomParser,
                               Collection<ScrappingHandler> scrappingHandlers,
                               Collection<FeedScrapperPlugin> scrapperPlugins,
                               AuthenticationFacade authFacade,
                               ScrapperProperties properties) {
        Map<String, FeedScrapperPlugin> plugins = scrapperPlugins.stream()
                .collect(Collectors.toUnmodifiableMap(FeedScrapperPlugin::pluginForDomain, Function.identity()));
        this.startScrapper = properties.start();
        this.scrapper = new FeedScrapperService(
                properties, feedPersistence, newsPersistence, authFacade,
                rssAtomParser, ogScrapper, scrappingHandlers, plugins);
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
