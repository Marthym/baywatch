package fr.ght1pc9kc.baywatch.infra.adapters;

import fr.ght1pc9kc.baywatch.api.scrapper.ScrappingHandler;
import fr.ght1pc9kc.baywatch.api.scrapper.RssAtomParser;
import fr.ght1pc9kc.baywatch.domain.ports.FeedPersistencePort;
import fr.ght1pc9kc.baywatch.domain.ports.NewsPersistencePort;
import fr.ght1pc9kc.baywatch.domain.scrapper.FeedScrapperService;
import fr.ght1pc9kc.baywatch.domain.scrapper.opengraph.OpenGraphScrapper;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.time.Duration;
import java.util.Collection;

@Service
@DependsOn({"flyway", "flywayInitializer"}) // Wait after Flyway migrations
public class FeedScrapperAdapter {
    private final FeedScrapperService scrapper;
    private final boolean startScrapper;

    public FeedScrapperAdapter(FeedPersistencePort feedPersistence, NewsPersistencePort newsPersistence,
                               OpenGraphScrapper ogScrapper, RssAtomParser rssAtomParser,
                               Collection<ScrappingHandler> scrappingHandlers,
                               @Value("${baywatch.scrapper.start}") boolean startScrapper,
                               @Value("${baywatch.scrapper.frequency}") Duration scrapFrequency) {
        this.startScrapper = startScrapper;
        this.scrapper = new FeedScrapperService(scrapFrequency,
                ogScrapper, feedPersistence, newsPersistence, rssAtomParser, scrappingHandlers);
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
