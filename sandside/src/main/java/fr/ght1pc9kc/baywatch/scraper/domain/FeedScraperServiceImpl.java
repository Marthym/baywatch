package fr.ght1pc9kc.baywatch.scraper.domain;

import fr.ght1pc9kc.baywatch.common.api.HttpHeaders;
import fr.ght1pc9kc.baywatch.common.api.HttpStatusCodes;
import fr.ght1pc9kc.baywatch.common.domain.DateUtils;
import fr.ght1pc9kc.baywatch.common.domain.Try;
import fr.ght1pc9kc.baywatch.scraper.api.FeedScraperPlugin;
import fr.ght1pc9kc.baywatch.scraper.api.FeedScraperService;
import fr.ght1pc9kc.baywatch.scraper.api.RssAtomParser;
import fr.ght1pc9kc.baywatch.scraper.api.ScrapEnrichmentService;
import fr.ght1pc9kc.baywatch.scraper.api.model.AtomFeed;
import fr.ght1pc9kc.baywatch.scraper.api.model.ScrapResult;
import fr.ght1pc9kc.baywatch.scraper.domain.model.ScrapedFeed;
import fr.ght1pc9kc.baywatch.scraper.api.ScrapingEventHandler;
import fr.ght1pc9kc.baywatch.scraper.domain.model.ex.FeedScrapingException;
import fr.ght1pc9kc.baywatch.scraper.domain.model.ex.ScrapingException;
import fr.ght1pc9kc.baywatch.scraper.domain.ports.ScraperMaintenancePort;
import fr.ght1pc9kc.baywatch.techwatch.api.model.News;
import fr.ght1pc9kc.baywatch.techwatch.api.model.State;
import fr.ght1pc9kc.entity.api.Entity;
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
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static fr.ght1pc9kc.baywatch.common.api.model.FeedMeta.ETag;
import static fr.ght1pc9kc.baywatch.common.api.model.FeedMeta.updated;

@Slf4j
public final class FeedScraperServiceImpl implements FeedScraperService {
    private static final Set<String> SUPPORTED_SCHEMES = Set.of("http", "https");
    private static final int MAX_PARALLEL_SCRAPERS = 4;

    private final Scheduler scraperScheduler;
    private final ScraperMaintenancePort maintenancePersistencePort;
    private final WebClient http;
    private final RssAtomParser feedParser;
    private final Collection<ScrapingEventHandler> scrapingHandlers;
    private final Map<String, FeedScraperPlugin> plugins;
    private final ScrapEnrichmentService scrapEnrichmentService;
    private final XmlEventDecoder xmlEventDecoder;

    @Setter(value = AccessLevel.PACKAGE, onMethod = @__({@VisibleForTesting}))
    private Clock clock = Clock.systemUTC();

    public FeedScraperServiceImpl(Scheduler scraperScheduler,
                                  ScraperMaintenancePort maintenancePersistencePort,
                                  WebClient webClient, RssAtomParser feedParser,
                                  Collection<ScrapingEventHandler> scrapingHandlers,
                                  Map<String, FeedScraperPlugin> plugins,
                                  ScrapEnrichmentService scrapEnrichmentService
    ) {
        this.scraperScheduler = scraperScheduler;
        this.maintenancePersistencePort = maintenancePersistencePort;
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
            Mono<Set<String>> alreadyHave = maintenancePersistencePort.listAllNewsId()
                    .collect(Collectors.toUnmodifiableSet())
                    .cache();

            Sinks.Many<ScrapingException> errors = Sinks.many().unicast().onBackpressureBuffer();
            Sinks.Many<Entity<AtomFeed>> updatedFeeds = Sinks.many().unicast().onBackpressureBuffer();

            return Flux.concat(scrapingHandlers.stream().map(ScrapingEventHandler::before).toList())
                    .thenMany(maintenancePersistencePort.feedList())
                    .parallel(MAX_PARALLEL_SCRAPERS).runOn(scraperScheduler)
                    .concatMap(feed -> wgetFeedNews(feed, maxRetention, errors, updatedFeeds))
                    .sequential()

                    .filterWhen(n -> alreadyHave.map(l -> !l.contains(n.id())))
                    .parallel(MAX_PARALLEL_SCRAPERS).runOn(scraperScheduler)
                    .concatMap(scrapEnrichmentService::applyNewsFilters)
                    .sequential()

                    .flatMap(tn -> this.handleEnrichmentException(tn, errors))
                    .filterWhen(n -> alreadyHave.map(l -> !l.contains(n.id())))
                    .buffer(100)
                    .flatMap(maintenancePersistencePort::newsLoad)

                    .reduce(Integer::sum)
                    .onErrorMap(t -> new ScrapingException("Fatal error when scraping !", t))
                    .doOnTerminate(() -> {
                        errors.tryEmitComplete();
                        updatedFeeds.tryEmitComplete();
                    })

                    .switchIfEmpty(Mono.just(0))

                    .flatMap(count -> this.updateFeeds(updatedFeeds.asFlux())
                            .then(Mono.just(count)))

                    .flatMap(count -> errors.asFlux().collectList().map(
                            collectedErrors -> new ScrapResult(count, collectedErrors)))
                    .flatMap(result -> Flux.concat(scrapingHandlers.stream().map(h -> h.after(result)).toList())
                            .then(Mono.just(result)))
                    .doFinally(signal -> scrapingHandlers.forEach(ScrapingEventHandler::onTerminate));
        } catch (Exception e) {
            return Mono.error(() -> new ScrapingException("Fatal error when scraping !", e));
        }
    }

    private Mono<Void> updateFeeds(Flux<Entity<AtomFeed>> toBeUpdated) {
        return toBeUpdated.groupBy(Entity::id)
                .flatMap(g -> g.reduce(AtomFeedReducer::reduce))
                .flatMap(original -> scrapEnrichmentService.applyFeedsFilters(original.self())
                        .map(filtered -> AtomFeedReducer.reduce(original, Entity.identify(filtered).withId(original.id()))))
                .buffer(100)
                .flatMap(maintenancePersistencePort::feedsUpdate)
                .then();
    }

    @Override
    public void dispose() {
        scraperScheduler.dispose();
    }

    private Flux<News> wgetFeedNews(ScrapedFeed feed, Period conservation,
                                    Sinks.Many<ScrapingException> errors, Sinks.Many<Entity<AtomFeed>> updatedFeeds) {
        String feedHost = feed.link().getHost();
        URI feedUrl = Optional.ofNullable(plugins.get(feedHost))
                .map(hp -> hp.uriModifier(feed.link())).orElse(feed.link());

        if (!SUPPORTED_SCHEMES.contains(feedUrl.getScheme())) {
            errors.tryEmitNext(new FeedScrapingException(AtomFeed.of(feed.id(), feed.link()),
                    new IllegalArgumentException("Unsupported scheme for " + feedUrl + " !")));
            log.warn("Unsupported scheme for {} !", feedUrl);
            return Flux.empty();
        }

        log.atTrace().addArgument(feedHost)
                .log("Start scraping feed {} ...");

        final Instant maxAge = LocalDate.now(clock)
                .minus(conservation)
                .atTime(LocalTime.MAX)
                .toInstant(DateUtils.DEFAULT_ZONE_OFFSET);

        return http.get()
                .uri(feedUrl)
                .accept(MediaType.APPLICATION_ATOM_XML)
                .accept(MediaType.APPLICATION_RSS_XML)
                .acceptCharset(StandardCharsets.UTF_8)
                .headers(httpHeaders -> {
                    if (feed.eTag() != null) {
                        httpHeaders.add(HttpHeaders.IF_NONE_MATCH, feed.eTag());
                    }
                })
                .exchangeToFlux(response -> {
                    if (!response.statusCode().is2xxSuccessful() && response.statusCode().value() != HttpStatusCodes.NOT_MODIFIED) {
                        errors.tryEmitNext(new FeedScrapingException(
                                AtomFeed.of(feed.id(), feed.link()),
                                new IllegalArgumentException("Bad response status " + response.statusCode())
                        ));
                        return response.releaseBody()
                                .thenMany(Flux.empty());

                    } else if (response.statusCode().value() == HttpStatusCodes.NOT_MODIFIED) {
                        log.atDebug().addArgument(feedUrl).log("NOT_MODIFIED -> {}");
                        return response.releaseBody()
                                .thenMany(Flux.empty());
                    }

                    List<String> httpETags = response.headers().header(HttpHeaders.ETAG);
                    if (!httpETags.isEmpty()) {
                        updatedFeeds.tryEmitNext(Entity.identify(AtomFeed.of(feed.id(), feedUrl))
                                .meta(ETag, httpETags.getFirst())
                                .withId(feed.id()));
                    }

                    return this.xmlEventDecoder.decode(
                                    response.bodyToFlux(DataBuffer.class).switchOnFirst(this::cleanupStreamStart),
                                    ResolvableType.NONE, null, null)
                            .onErrorResume(RuntimeException.class, t -> {
                                errors.tryEmitNext(new FeedScrapingException(AtomFeed.of(feed.id(), feed.link()), t));
                                return response.releaseBody().thenReturn(XMLEventFactory.newDefaultFactory().createEndDocument());
                            });
                })
                .doFirst(() -> log.trace("Receiving event from {}...", feedHost))

                .bufferUntil(feedParser.itemEndEvent())
                .switchOnFirst((first, others) -> {
                    if (!first.hasValue()) {
                        return others.take(0).thenMany(Flux.empty());
                    }
                    AtomFeed atomFeed = feedParser.readFeedProperties(first.get()).toBuilder()
                            .id(feed.id())
                            .link(feedUrl)
                            .build();
                    if (atomFeed.updated() != null && !atomFeed.updated().isAfter(feed.updated())) {
                        return others.take(0).thenMany(Flux.empty());
                    } else {
                        updatedFeeds.tryEmitNext(Entity.identify(atomFeed)
                                .meta(updated, atomFeed.updated())
                                .withId(feed.id()));
                        return others;
                    }
                })

                .flatMap(entry -> feedParser.readEntryEvents(entry, feed))
                .filter(news -> news.publication().isAfter(maxAge))
                .map(raw -> News.builder()
                        .raw(raw)
                        .feeds(Set.of(feed.id()))
                        .state(State.NONE)
                        .build())

                .onErrorResume(e -> {
                    errors.tryEmitNext(new FeedScrapingException(AtomFeed.of(feed.id(), feed.link()), e));
                    return Flux.empty();
                })
                .doFinally(s -> log.atDebug().addArgument(feed.link())
                        .log("Finish reading feed {}"));
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

    private Mono<News> handleEnrichmentException(Try<News> news, Sinks.Many<ScrapingException> errors) {
        if (news.isFailure()) {
            if (news.getCause() instanceof ScrapingException cause) {
                errors.tryEmitNext(cause);
            } else {
                errors.tryEmitNext(new ScrapingException("Unknown entity scraping error !", news.getCause()));
            }
            return Mono.empty();
        } else {
            return Mono.just(news.get());
        }
    }
}
