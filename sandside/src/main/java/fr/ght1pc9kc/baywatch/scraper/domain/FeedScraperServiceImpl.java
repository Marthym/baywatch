package fr.ght1pc9kc.baywatch.scraper.domain;

import fr.ght1pc9kc.baywatch.common.api.ScrapingEventHandler;
import fr.ght1pc9kc.baywatch.common.domain.DateUtils;
import fr.ght1pc9kc.baywatch.scraper.api.FeedScraperPlugin;
import fr.ght1pc9kc.baywatch.scraper.api.FeedScraperService;
import fr.ght1pc9kc.baywatch.scraper.api.RssAtomParser;
import fr.ght1pc9kc.baywatch.scraper.api.ScrapEnrichmentService;
import fr.ght1pc9kc.baywatch.scraper.api.model.AtomFeed;
import fr.ght1pc9kc.baywatch.scraper.api.model.ScrapResult;
import fr.ght1pc9kc.baywatch.scraper.api.model.ScrapingError;
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
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.core.io.buffer.DefaultDataBufferFactory;
import org.springframework.http.MediaType;
import org.springframework.http.codec.xml.XmlEventDecoder;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Signal;
import reactor.core.publisher.Sinks;
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
    private static final Set<String> SUPPORTED_SCHEMES = Set.of("http", "https");

    private final Scheduler scraperScheduler;
    private final NewsMaintenancePort newsMaintenance;
    private final WebClient http;
    private final RssAtomParser feedParser;
    private final Collection<ScrapingEventHandler> scrapingHandlers;
    private final Map<String, FeedScraperPlugin> plugins;
    private final ScrapEnrichmentService scrapEnrichmentService;
    private final XmlEventDecoder xmlEventDecoder;

    @Setter(value = AccessLevel.PACKAGE, onMethod = @__({@VisibleForTesting}))
    private Clock clock = Clock.systemUTC();

    public FeedScraperServiceImpl(Scheduler scraperScheduler,
                                  NewsMaintenancePort newsMaintenance,
                                  WebClient webClient, RssAtomParser feedParser,
                                  Collection<ScrapingEventHandler> scrapingHandlers,
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

            Sinks.Many<ScrapingError> errors = Sinks.many().unicast().onBackpressureBuffer();
            return Flux.concat(scrapingHandlers.stream().map(ScrapingEventHandler::before).toList())
                    .thenMany(newsMaintenance.feedList())
                    .parallel(4).runOn(scraperScheduler)
                    .concatMap(feed -> wgetFeedNews(feed, maxRetention, errors))
                    .sequential()

                    .filterWhen(n -> alreadyHave.map(l -> !l.contains(n.getId())))
                    .parallel(4).runOn(scraperScheduler)
                    .concatMap(scrapEnrichmentService::applyNewsFilters)
                    .sequential()

                    .filterWhen(n -> alreadyHave.map(l -> !l.contains(n.getId())))
                    .buffer(100)
                    .flatMap(newsMaintenance::newsLoad)

                    .reduce(Integer::sum)
                    .onErrorMap(t -> new ScrapingException("Fatal error when scraping !", t))

                    .switchIfEmpty(Mono.just(0))
                    .flatMap(count -> errors.asFlux().collectList().map(
                            collectedErrors -> new ScrapResult(count, collectedErrors)))
                    .flatMap(result -> Flux.concat(scrapingHandlers.stream().map(h -> h.after(result)).toList())
                            .then(Mono.just(result)))
                    .doFinally(signal -> scrapingHandlers.forEach(ScrapingEventHandler::onTerminate));
        } catch (Exception e) {
            return Mono.error(() -> new ScrapingException("Fatal error when scraping !", e));
        }
    }

    @Override
    public void dispose() {
        scraperScheduler.dispose();
    }

    private Flux<News> wgetFeedNews(ScrapedFeed feed, Period conservation, Sinks.Many<ScrapingError> errors) {
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
                        errors.tryEmitNext(new ScrapingError(
                                feed.link().toString(),
                                new IllegalArgumentException("Bad response status " + response.statusCode())
                        ));
                        return Flux.empty();
                    }
                    return this.xmlEventDecoder.decode(
                                    response.bodyToFlux(DataBuffer.class).switchOnFirst(this::cleanupStreamStart),
                                    ResolvableType.NONE, null, null)
                            .onErrorResume(RuntimeException.class, t -> {
                                errors.tryEmitNext(new ScrapingError(feed.link().toString(), t));
                                return response.releaseBody().thenReturn(XMLEventFactory.newDefaultFactory().createEndDocument());
                            });
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

                .onErrorResume(e -> {
                    errors.tryEmitNext(new ScrapingError(feed.link().toString(), e));
                    return Flux.empty();
                })
                .doFinally(s -> {
                    log.debug("Finish reading feed {}", feedHost);
                    errors.tryEmitComplete();
                });
    }

    private Flux<DataBuffer> cleanupStreamStart(Signal<? extends DataBuffer> signal, Flux<DataBuffer> source) {
        DataBuffer buffer = signal.get();
        if (!signal.hasValue() || buffer == null) {
            return source;
        }
        String bufferString = buffer.toString(StandardCharsets.UTF_8);
        DataBufferFactory bufferFactory = new DefaultDataBufferFactory();
        int startIdx = bufferString.indexOf("<?xml ");
        if (startIdx < 0) {
            startIdx = bufferString.indexOf("<rss ");
        }
        if (startIdx < 0) {
            startIdx = bufferString.indexOf("<feed ");
        }
        if (startIdx > 0) {
            DataBufferUtils.release(buffer);
            return source.skip(1)
                    .startWith(bufferFactory.wrap(bufferString.substring(startIdx).getBytes(StandardCharsets.UTF_8)));
        } else {
            return source;
        }
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
