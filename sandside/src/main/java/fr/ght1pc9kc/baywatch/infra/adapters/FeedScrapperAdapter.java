package fr.ght1pc9kc.baywatch.infra.adapters;

import fr.ght1pc9kc.baywatch.api.scrapper.FeedScrapperPlugin;
import fr.ght1pc9kc.baywatch.api.scrapper.RssAtomParser;
import fr.ght1pc9kc.baywatch.api.scrapper.ScrappingHandler;
import fr.ght1pc9kc.baywatch.domain.ports.AuthenticationFacade;
import fr.ght1pc9kc.baywatch.domain.scrapper.FeedScrapperService;
import fr.ght1pc9kc.baywatch.domain.scrapper.opengraph.OpenGraphScrapper;
import fr.ght1pc9kc.baywatch.domain.techwatch.ports.FeedPersistencePort;
import fr.ght1pc9kc.baywatch.domain.techwatch.ports.NewsPersistencePort;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.time.Duration;
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
                               OpenGraphScrapper ogScrapper, RssAtomParser rssAtomParser,
                               Collection<ScrappingHandler> scrappingHandlers,
                               Collection<FeedScrapperPlugin> scrapperPlugins,
                               AuthenticationFacade authFacade,
                               @Value("${baywatch.scrapper.start}") boolean startScrapper,
                               @Value("${baywatch.scrapper.frequency}") Duration scrapFrequency) {
        Map<String, FeedScrapperPlugin> plugins = scrapperPlugins.stream()
                .collect(Collectors.toUnmodifiableMap(FeedScrapperPlugin::pluginForDomain, Function.identity()));
        this.startScrapper = startScrapper;
        this.scrapper = new FeedScrapperService(
                scrapFrequency, feedPersistence, newsPersistence, authFacade,
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
