package fr.ght1pc9kc.baywatch.techwatch.infra.adapters;

import fr.ght1pc9kc.baywatch.scraper.api.FeedScraperService;
import fr.ght1pc9kc.baywatch.techwatch.api.model.Feed;
import fr.ght1pc9kc.baywatch.techwatch.domain.ports.ScraperServicePort;
import fr.ght1pc9kc.baywatch.techwatch.infra.config.TechwatchMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.net.URI;

@Component
@RequiredArgsConstructor
public class ScraperServiceAdapter implements ScraperServicePort {
    private final FeedScraperService feedScraperService;
    private final TechwatchMapper mapper;

    @Override
    public Mono<Feed> fetchFeedData(URI link) {
        return feedScraperService.scrapFeedHeader(link)
                .map(mapper::getFeedFromAtom);
    }
}
