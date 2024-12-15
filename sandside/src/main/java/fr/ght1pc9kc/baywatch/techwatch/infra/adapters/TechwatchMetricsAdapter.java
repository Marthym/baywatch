package fr.ght1pc9kc.baywatch.techwatch.infra.adapters;

import fr.ght1pc9kc.baywatch.common.domain.QueryContext;
import fr.ght1pc9kc.baywatch.security.api.AuthenticationFacade;
import fr.ght1pc9kc.baywatch.techwatch.infra.persistence.FeedRepository;
import fr.ght1pc9kc.baywatch.techwatch.infra.persistence.NewsRepository;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicInteger;

@Component
@RequiredArgsConstructor
public class TechwatchMetricsAdapter {
    private final MeterRegistry registry;
    private final NewsRepository newsRepository;
    private final FeedRepository feedRepository;

    private final AtomicInteger newsCount = new AtomicInteger();
    private final AtomicInteger feedsCount = new AtomicInteger();

    @PostConstruct
    public void postConstruct() {
        Mono<Integer> newsCountEvent = newsRepository.count(QueryContext.empty())
                .contextWrite(AuthenticationFacade.withSystemAuthentication())
                .cache(Duration.ofMinutes(10));

        Mono<Integer> feedsCountEvent = feedRepository.count(QueryContext.empty())
                .contextWrite(AuthenticationFacade.withSystemAuthentication())
                .cache(Duration.ofMinutes(10));

        Gauge.builder("bw.news.count", () -> {
                    newsCountEvent.subscribe(newsCount::set);
                    return newsCount.get();
                }).description("Total number of News")
                .register(registry);

        Gauge.builder("bw.feeds.count", () -> {
                    feedsCountEvent.subscribe(feedsCount::set);
                    return feedsCount.get();
                }).description("Total number of Feeds")
                .register(registry);
    }
}
