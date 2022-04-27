package fr.ght1pc9kc.baywatch.scrapper.domain;

import fr.ght1pc9kc.baywatch.common.domain.DateUtils;
import fr.ght1pc9kc.baywatch.scrapper.api.FeedScrapperPlugin;
import fr.ght1pc9kc.baywatch.scrapper.api.RssAtomParser;
import fr.ght1pc9kc.baywatch.scrapper.api.ScrappingHandler;
import fr.ght1pc9kc.baywatch.scrapper.infra.config.ScrapperProperties;
import fr.ght1pc9kc.baywatch.security.api.AuthenticationFacade;
import fr.ght1pc9kc.baywatch.techwatch.api.model.Feed;
import fr.ght1pc9kc.baywatch.techwatch.api.model.News;
import fr.ght1pc9kc.baywatch.techwatch.api.model.RawNews;
import fr.ght1pc9kc.baywatch.techwatch.domain.ports.FeedPersistencePort;
import fr.ght1pc9kc.baywatch.techwatch.domain.ports.NewsPersistencePort;
import fr.ght1pc9kc.scraphead.core.HeadScraper;
import fr.ght1pc9kc.scraphead.core.model.links.Links;
import fr.ght1pc9kc.scraphead.core.model.opengraph.OpenGraph;
import io.netty.channel.ChannelOption;
import io.netty.channel.epoll.EpollDatagramChannel;
import io.netty.resolver.dns.DnsAddressResolverGroup;
import io.netty.resolver.dns.DnsNameResolverBuilder;
import io.netty.resolver.dns.SequentialDnsServerAddressStreamProvider;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.VisibleForTesting;
import org.springframework.core.ResolvableType;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.http.codec.xml.XmlEventDecoder;
import org.springframework.scheduling.concurrent.CustomizableThreadFactory;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;
import reactor.netty.http.client.HttpClient;

import java.net.InetSocketAddress;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static java.util.Objects.nonNull;

@Slf4j
public final class FeedScrapperService implements Runnable {

    private static final Set<String> SUPPORTED_SCHEMES = Set.of("http", "https");
    public static final String ERROR_CLASS_MESSAGE = "{}: {}";
    public static final String ERROR_STACKTRACE_MESSAGE = "STACKTRACE";

    private final ScheduledExecutorService scheduleExecutor = Executors.newSingleThreadScheduledExecutor(
            new CustomizableThreadFactory("scrapSched-"));
    private final Scheduler scrapperScheduler =
            Schedulers.newBoundedElastic(5, Integer.MAX_VALUE, "scrapper");
    private final WebClient http;
    private final Clock clock = Clock.systemUTC();
    private final Semaphore lock = new Semaphore(1);

    private final ScrapperProperties properties;
    private final FeedPersistencePort feedRepository;
    private final NewsPersistencePort newsRepository;
    private final AuthenticationFacade authFacade;
    private final RssAtomParser feedParser;
    private final HeadScraper headScrapper;
    private final Collection<ScrappingHandler> scrappingHandlers;
    private final Map<String, FeedScrapperPlugin> plugins;
    private final XmlEventDecoder xmlEventDecoder;

    public FeedScrapperService(ScrapperProperties properties,
                               FeedPersistencePort feedRepository, NewsPersistencePort newsRepository,
                               AuthenticationFacade authFacade,
                               RssAtomParser feedParser, HeadScraper headScrapper,
                               Collection<ScrappingHandler> scrappingHandlers,
                               Map<String, FeedScrapperPlugin> plugins) {
        this.properties = properties;
        this.feedRepository = feedRepository;
        this.newsRepository = newsRepository;
        this.authFacade = authFacade;
        this.feedParser = feedParser;
        this.headScrapper = headScrapper;
        this.scrappingHandlers = scrappingHandlers;
        this.plugins = plugins;

        DnsAddressResolverGroup dnsAddressResolverGroup =
                new DnsAddressResolverGroup(
                        new DnsNameResolverBuilder()
                                .queryTimeoutMillis(10_000)
                                .channelType(EpollDatagramChannel.class)
                                .nameServerProvider(
                                        new SequentialDnsServerAddressStreamProvider(
                                                new InetSocketAddress("9.9.9.9", 53),
                                                new InetSocketAddress("8.8.8.8", 53))));


        this.http = WebClient.builder()
                .clientConnector(new ReactorClientHttpConnector(
                        HttpClient.create()
                                .resolver(dnsAddressResolverGroup)
                                .followRedirect(true)
                                .compress(true)
                                .responseTimeout(properties.timeout())
                                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, (int) properties.timeout().toMillis())
                )).build();
        this.xmlEventDecoder = new XmlEventDecoder();
        this.xmlEventDecoder.setMaxInMemorySize(16 * 1024 * 1024);
    }

    public void startScrapping() {
        Instant now = clock.instant();
        Instant nextScrapping = now.plus(properties.frequency());
        Duration toNextScrapping = Duration.between(now, nextScrapping);

        scheduleExecutor.scheduleAtFixedRate(this,
                toNextScrapping.getSeconds(), properties.frequency().getSeconds(), TimeUnit.SECONDS);
        log.debug("Next scrapping at {}", LocalDateTime.now().plus(toNextScrapping));
        scheduleExecutor.schedule(this, 0, TimeUnit.MILLISECONDS);
    }

    @SneakyThrows
    public void shutdownScrapping() {
        if (!lock.tryAcquire(60, TimeUnit.SECONDS)) {
            log.warn("Unable to stop threads gracefully ! Threads was killed !");
        }
        scrapperScheduler.dispose();
        scheduleExecutor.shutdownNow();
        lock.release();
        log.info("All scrapper tasks finished and stopped !");
    }

    @Override
    @SneakyThrows
    public void run() {
        if (!lock.tryAcquire()) {
            log.warn("Scrapping in progress !");
        }
        log.info("Start scrapping ...");
        Mono<Set<String>> alreadyHave = newsRepository.list()
                .map(News::getId)
                .collect(Collectors.toUnmodifiableSet())
                .cache();

        Flux.concat(scrappingHandlers.stream().map(ScrappingHandler::before).toList())
                .thenMany(feedRepository.list())
                .parallel(4).runOn(scrapperScheduler)
                .concatMap(this::wgetFeedNews)
                .sequential()
                .filterWhen(n -> alreadyHave.map(l -> !l.contains(n.getId())))
                .parallel(4).runOn(scrapperScheduler)
                .concatMap(this::completeWithOpenGraph)
                .sequential()
                .buffer(100)
                .flatMap(newsRepository::persist)
                .reduce(Integer::sum)
                .flatMap(count -> Flux.concat(scrappingHandlers.stream().map(h -> h.after(count)).toList()).then())
                .doOnError(e -> {
                    log.error(ERROR_CLASS_MESSAGE, e.getClass(), e.getLocalizedMessage());
                    log.debug(ERROR_STACKTRACE_MESSAGE, e);
                })
                .doFinally(signal -> {
                    lock.release();
                    log.info("Scraping terminated successfully !");
                })
                .contextWrite(authFacade.withSystemAuthentication())
                .subscribe();
    }

    @SuppressWarnings("java:S2095")
    private Flux<News> wgetFeedNews(Feed feed) {
        String feedHost = feed.getUrl().getHost();
        FeedScrapperPlugin hostPlugin = plugins.get(feedHost);
        URI feedUrl = (hostPlugin != null) ? hostPlugin.uriModifier(feed.getUrl()) : feed.getUrl();

        log.debug("Start scraping feed {} ...", feedHost);
//            PipedOutputStream osPipe = new PipedOutputStream();
//            PipedInputStream isFeedPayload = new PipedInputStream(osPipe);

        final Instant maxAge = DateUtils.toInstant(DateUtils.toLocalDate(clock.instant()).minus(properties.conservation()));
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
                            response.bodyToFlux(DataBuffer.class).publishOn(scrapperScheduler),
                            ResolvableType.NONE, null, null);
                })
                .doFirst(() -> log.trace("Receiving event from {}...", feedHost))
                .skipUntil(e -> e.isStartElement() && (
                        "item".equals(e.asStartElement().getName().getLocalPart())
                                || "entry".equals(e.asStartElement().getName().getLocalPart())))
                .bufferUntil(e -> e.isEndElement() && (
                        "item".equals(e.asEndElement().getName().getLocalPart())
                                || "entry".equals(e.asEndElement().getName().getLocalPart())))
                .flatMap(entry -> feedParser.readEntryEvents(entry, feed))
                .takeUntil(news -> news.getPublication().isBefore(maxAge))
                .doFinally(s -> log.debug("Finish scraping feed {}", feedHost))
                .onErrorResume(e -> {
                    log.warn(ERROR_CLASS_MESSAGE, feedHost, e.getLocalizedMessage());
                    log.debug(ERROR_STACKTRACE_MESSAGE, e);
                    return Flux.empty();
                });
    }

    private Mono<News> completeWithOpenGraph(News news) {
        try {
            return headScrapper.scrap(news.getLink())
                    .map(headMetas -> {
                        RawNews raw = news.getRaw();

                        Links links = headMetas.links();
                        if (nonNull(links) && nonNull(links.canonical())) {
                            raw = raw.withLink(links.canonical());
                        }

                        OpenGraph og = headMetas.og();
                        if (og.isEmpty()) {
                            log.debug("No OG meta found for {}", news.getLink());
                            return (news.getRaw() == raw) ? news : news.withRaw(raw);
                        }
                        raw = Optional.ofNullable(og.title).map(raw::withTitle).orElse(raw);
                        raw = Optional.ofNullable(og.description).map(raw::withDescription).orElse(raw);
                        raw = Optional.ofNullable(og.image)
                                .filter(i -> SUPPORTED_SCHEMES.contains(i.getScheme()))
                                .map(raw::withImage).orElse(raw);
                        return (news.getRaw() == raw) ? news : news.withRaw(raw);
                    }).switchIfEmpty(Mono.just(news));
        } catch (Exception e) {
            log.warn("Unable to scrap header from {}.", news.getLink());
            log.debug(ERROR_STACKTRACE_MESSAGE, e);
            return Mono.just(news);
        }
    }

    @VisibleForTesting
    public boolean isScrapping() {
        return lock.availablePermits() == 0;
    }
}
