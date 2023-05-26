package fr.ght1pc9kc.baywatch.scraper.domain;

import fr.ght1pc9kc.baywatch.common.api.EventHandler;
import fr.ght1pc9kc.baywatch.common.domain.DateUtils;
import fr.ght1pc9kc.baywatch.scraper.api.FeedScraperPlugin;
import fr.ght1pc9kc.baywatch.scraper.api.FeedScraperService;
import fr.ght1pc9kc.baywatch.scraper.api.RssAtomParser;
import fr.ght1pc9kc.baywatch.scraper.api.ScrapEnrichmentService;
import fr.ght1pc9kc.baywatch.scraper.api.model.AtomFeed;
import fr.ght1pc9kc.baywatch.scraper.api.model.ScrapResult;
import fr.ght1pc9kc.baywatch.scraper.api.model.ScrapingException;
import fr.ght1pc9kc.baywatch.scraper.domain.model.ScrapedFeed;
import fr.ght1pc9kc.baywatch.scraper.domain.ports.NewsMaintenancePort;
import fr.ght1pc9kc.baywatch.techwatch.api.model.News;
import fr.ght1pc9kc.baywatch.techwatch.api.model.State;
import lombok.AccessLevel;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.VisibleForTesting;
import org.springframework.core.ResolvableType;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.MediaType;
import org.springframework.http.codec.xml.XmlEventDecoder;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;

import javax.xml.stream.XMLEventFactory;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.Period;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
public final class FeedScraperServiceImpl implements FeedScraperService {
    public static final String ERROR_CLASS_MESSAGE = "{}: {}";
    public static final String ERROR_STACKTRACE_MESSAGE = "STACKTRACE";
    private static final Set<String> SUPPORTED_SCHEMES = Set.of("http", "https");

    private final Scheduler scraperScheduler;
    private final NewsMaintenancePort newsMaintenance;
    private final WebClient http;
    private final RssAtomParser feedParser;
    private final Collection<EventHandler> scrapingHandlers;
    private final Map<String, FeedScraperPlugin> plugins;
    private final ScrapEnrichmentService scrapEnrichmentService;
    private final XmlEventDecoder xmlEventDecoder;

    @Setter(value = AccessLevel.PACKAGE, onMethod = @__({@VisibleForTesting}))
    private Clock clock = Clock.systemUTC();

    public FeedScraperServiceImpl(Scheduler scraperScheduler,
                                  NewsMaintenancePort newsMaintenance,
                                  WebClient webClient, RssAtomParser feedParser,
                                  Collection<EventHandler> scrapingHandlers,
                                  Map<String, FeedScraperPlugin> plugins,
                                  ScrapEnrichmentService scrapEnrichmentService
    ) {
        this.scraperScheduler = scraperScheduler;
        this.newsMaintenance = newsMaintenance;
        this.feedParser = feedParser;
        this.scrapingHandlers = scrapingHandlers.stream()
                .filter(e -> e.eventTypes().contains("FEED_SCRAPING")).toList();
        this.plugins = plugins;
        this.scrapEnrichmentService = scrapEnrichmentService;
        this.http = webClient;

        this.xmlEventDecoder = new XmlEventDecoder();
        this.xmlEventDecoder.setMaxInMemorySize(16 * 1024 * 1024);
    }

    @Override
    public Mono<ScrapResult> scrap(Period maxRetention) {
        try {
            Mono<Set<String>> alreadyHave = newsMaintenance.listAllNewsId()
                    .collect(Collectors.toUnmodifiableSet())
                    .cache();

            return Flux.concat(scrapingHandlers.stream().map(EventHandler::before).toList())
                    .thenMany(newsMaintenance.feedList())
                    .parallel(4).runOn(scraperScheduler)
                    .concatMap(feed -> wgetFeedNews(feed, maxRetention))
                    .sequential()

                    .filterWhen(n -> alreadyHave.map(l -> !l.contains(n.getId())))
                    .parallel(4).runOn(scraperScheduler)
                    .concatMap(scrapEnrichmentService::applyNewsFilters)
                    .sequential()

                    .filterWhen(n -> alreadyHave.map(l -> !l.contains(n.getId())))
                    .buffer(100)
                    .flatMap(newsMaintenance::newsLoad)

                    .reduce(Integer::sum)
                    .flatMap(count -> Flux.concat(scrapingHandlers.stream().map(h -> h.after(count)).toList())
                            .then(Mono.just(count)))
                    .map(count -> new ScrapResult(count, 0))
                    .onErrorMap(t -> new ScrapingException("Fatal error when scraping !", t))
                    .doFinally(signal -> scrapingHandlers.forEach(EventHandler::onTerminate));
        } catch (Exception e) {
            return Mono.error(() -> new ScrapingException("Fatal error when scraping !", e));
        }
    }

    @Override
    public void dispose() {
        scraperScheduler.dispose();
    }

    private Flux<News> wgetFeedNews(ScrapedFeed feed, Period conservation) {
        String feedHost = feed.link().getHost();
        FeedScraperPlugin hostPlugin = plugins.get(feedHost);
        URI feedUrl = (hostPlugin != null) ? hostPlugin.uriModifier(feed.link()) : feed.link();

        if (!SUPPORTED_SCHEMES.contains(feedUrl.getScheme())) {
            log.warn("Unsupported scheme for {} !", feedUrl);
            return Flux.empty();
        }

        log.debug("Start scraping feed {} ...", feedHost);

        final Instant maxAge = LocalDate.now(clock)
                .minus(conservation)
                .atTime(LocalTime.MAX)
                .toInstant(DateUtils.DEFAULT_ZONE_OFFSET);

        return http.get()
                .uri(feedUrl)
                .accept(MediaType.APPLICATION_ATOM_XML)
                .accept(MediaType.APPLICATION_RSS_XML)
                .acceptCharset(StandardCharsets.UTF_8)
                .exchangeToFlux(response -> {
                    if (!response.statusCode().is2xxSuccessful()) {
                        log.info("Host {} respond {}", feedHost, response.statusCode());
                        return Flux.empty();
                    }
                    return this.xmlEventDecoder.decode(
                                    response.bodyToFlux(DataBuffer.class),
                                    ResolvableType.NONE, null, null)
                            .doOnError(t -> {
                                log.warn("Error while decoding {}", feed.link());
                                log.debug(ERROR_CLASS_MESSAGE,
                                        t.getClass(), t.getLocalizedMessage().lines().findFirst().orElse(t.getLocalizedMessage()));
                            })
                            .onErrorReturn(RuntimeException.class, XMLEventFactory.newDefaultFactory().createEndDocument());
                })
                .doFirst(() -> log.trace("Receiving event from {}...", feedHost))

                .skipUntil(feedParser.firstItemEvent())
                .bufferUntil(feedParser.itemEndEvent())
                .flatMap(entry -> feedParser.readEntryEvents(entry, feed))
                .takeWhile(news -> news.getPublication().isAfter(maxAge))
                .map(raw -> News.builder()
                        .raw(raw)
                        .feeds(Set.of(feed.id()))
                        .state(State.NONE)
                        .build())

                .doFinally(s -> log.debug("Finish reading feed {}", feedHost))
                .onErrorResume(e -> {
                    log.warn(ERROR_CLASS_MESSAGE, feed.link(), e.getLocalizedMessage());
                    log.debug(ERROR_STACKTRACE_MESSAGE, e);
                    return Flux.empty();
                });
    }

    @Override
    public Mono<AtomFeed> scrapFeedHeader(URI link) {
        return http.get()
                .uri(link)
                .accept(MediaType.APPLICATION_ATOM_XML)
                .accept(MediaType.APPLICATION_RSS_XML)
                .acceptCharset(StandardCharsets.UTF_8)
                .exchangeToFlux(response -> {
                    if (!response.statusCode().is2xxSuccessful()) {
                        log.info("Host {} respond {}", link.getHost(), response.statusCode());
                        return Flux.empty();
                    }
                    return this.xmlEventDecoder.decode(
                            response.bodyToFlux(DataBuffer.class),
                            ResolvableType.NONE, null, null);
                })

                .bufferUntil(feedParser.firstItemEvent())
                .next()
                .map(feedParser::readFeedProperties)
                .flatMap(scrapEnrichmentService::applyFeedsFilters);
    }
}
