package fr.ght1pc9kc.baywatch.scraper.domain;

import fr.ght1pc9kc.baywatch.common.domain.Hasher;
import fr.ght1pc9kc.baywatch.common.domain.Try;
import fr.ght1pc9kc.baywatch.scraper.api.RssAtomParser;
import fr.ght1pc9kc.baywatch.scraper.api.ScrapEnrichmentService;
import fr.ght1pc9kc.baywatch.scraper.api.model.AtomEntry;
import fr.ght1pc9kc.baywatch.scraper.api.model.AtomFeed;
import fr.ght1pc9kc.baywatch.scraper.api.model.ScrapResult;
import fr.ght1pc9kc.baywatch.scraper.domain.model.ScrapedFeed;
import fr.ght1pc9kc.baywatch.scraper.domain.model.ex.FeedScrapingException;
import fr.ght1pc9kc.baywatch.scraper.domain.model.ex.NewsScrapingException;
import fr.ght1pc9kc.baywatch.scraper.domain.ports.ScraperMaintenancePort;
import fr.ght1pc9kc.baywatch.techwatch.api.model.News;
import fr.ght1pc9kc.baywatch.techwatch.api.model.RawNews;
import org.assertj.core.api.Assertions;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.stubbing.Answer;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.core.io.buffer.DefaultDataBufferFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.ExchangeFunction;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import reactor.test.StepVerifier;

import javax.xml.stream.events.XMLEvent;
import java.net.URI;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.Period;
import java.time.ZoneOffset;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class FeedScraperServiceTest {

    static final Period SCRAPER_RETENTION_PERIOD = Period.ofDays(30);

    private final ScrapEnrichmentService mockScrapEnrichmentService = mock(ScrapEnrichmentService.class);

    private FeedScraperServiceImpl tested;
    private RssAtomParser rssAtomParserMock;
    private ScraperMaintenancePort maintenancePersistencePort;
    private ExchangeFunction mockExchangeFunction;

    @Test
    void should_start_multi_scrapper() {
        StepVerifier.create(tested.scrap(SCRAPER_RETENTION_PERIOD))
                .expectNext(new ScrapResult(1, List.of()))
                .verifyComplete();

        verify(mockExchangeFunction).exchange(ArgumentMatchers.argThat(cr -> cr.url().getPath().equals("/feeds/journal_du_hacker.xml")));
        verify(mockExchangeFunction).exchange(ArgumentMatchers.argThat(cr -> cr.url().getPath().equals("/feeds/spring-blog.xml")));

        verify(rssAtomParserMock, times(2)).readEntryEvents(any(), any(ScrapedFeed.class));
        verify(maintenancePersistencePort,
                times(1).description("Expect only one call because of the buffer to 100")
        ).newsLoad(anyCollection());
        verify(mockScrapEnrichmentService, times(2)).applyNewsFilters(any());
    }

    @Test
    void should_scrap_bad_rss_feed() {
        URI sNumeriquesUrl = URI.create("https://www.jedi.com/feeds/malformed_rss_feed.xml");
        String sNumeriquesSha3 = Hasher.identify(sNumeriquesUrl);

        when(maintenancePersistencePort.feedList()).thenReturn(Flux.just(
                new ScrapedFeed(sNumeriquesSha3, sNumeriquesUrl, Instant.EPOCH, null)
        ));
        StepVerifier.create(tested.scrap(SCRAPER_RETENTION_PERIOD))
                .expectNext(new ScrapResult(1, List.of()))
                .verifyComplete();

        verify(mockExchangeFunction).exchange(ArgumentMatchers.argThat(cr -> cr.url().getPath().equals("/feeds/malformed_rss_feed.xml")));

        verify(rssAtomParserMock, times(1)).readEntryEvents(any(), any(ScrapedFeed.class));
        verify(maintenancePersistencePort,
                times(1).description("Expect only one call because of the buffer to 100")
        ).newsLoad(anyCollection());
        verify(mockScrapEnrichmentService, times(1)).applyNewsFilters(any());
    }

    @Test
    void should_ignore_unmodified_feeds() {
        URI springUri = URI.create("https://www.jedi.com/feeds/spring-blog.xml");
        String springSha3 = Hasher.identify(springUri);
        when(maintenancePersistencePort.feedList()).thenReturn(Flux.just(
                // The test sample WebFeed are updated at EPOCH + 1d
                new ScrapedFeed(springSha3, springUri, Instant.EPOCH.plus(Duration.ofDays(2)), null)
        ));
        StepVerifier.create(tested.scrap(SCRAPER_RETENTION_PERIOD))
                .expectNext(new ScrapResult(0, List.of()))
                .verifyComplete();

        verify(mockExchangeFunction).exchange(ArgumentMatchers.argThat(cr -> cr.url().getPath().equals("/feeds/spring-blog.xml")));

        verify(rssAtomParserMock, never()).readEntryEvents(any(), any(ScrapedFeed.class));
    }

    @Test
    void should_fail_scraper_without_fail_scraping() {
        URI darthVaderUri = URI.create("https://www.jedi.com/error/darth-vader.xml");
        URI springUri = URI.create("https://www.jedi.com/feeds/spring-blog.xml");
        String darthVaderSha3 = Hasher.identify(darthVaderUri);
        String springSha3 = Hasher.identify(springUri);

        when(maintenancePersistencePort.feedList()).thenReturn(Flux.just(
                new ScrapedFeed(darthVaderSha3, darthVaderUri, Instant.EPOCH, null),
                new ScrapedFeed(springSha3, springUri, Instant.EPOCH, null)
        ));

        StepVerifier.create(tested.scrap(SCRAPER_RETENTION_PERIOD))
                .assertNext(actual -> assertAll(
                        () -> Assertions.assertThat(actual.inserted()).isEqualTo(1),
                        () -> Assertions.assertThat(actual.errors()).isNotEmpty(),
                        () -> Assertions.assertThat(actual.errors())
                                .element(0).isInstanceOf(FeedScrapingException.class)
                                .extracting("cause").isInstanceOf(IllegalArgumentException.class)
                )).verifyComplete();

        verify(mockExchangeFunction).exchange(ArgumentMatchers.argThat(cr -> cr.url().getPath().equals("/error/darth-vader.xml")));
        verify(mockExchangeFunction).exchange(ArgumentMatchers.argThat(cr -> cr.url().getPath().equals("/feeds/spring-blog.xml")));

        verify(rssAtomParserMock, times(1)).readEntryEvents(any(), any(ScrapedFeed.class));
        verify(maintenancePersistencePort,
                times(1).description("Expect only one call because of the buffer of 100")
        ).newsLoad(anyCollection());
    }

    @Test
    void should_fail_enrichment_without_fail_scraping() {
        reset(mockExchangeFunction);
        when(mockScrapEnrichmentService.applyNewsFilters(any(News.class)))
                .thenAnswer(((Answer<Mono<Try<News>>>) answer -> Mono.just(Try.fail(new NewsScrapingException(
                        new AtomEntry(
                                answer.getArgument(0, News.class).id(),
                                answer.getArgument(0, News.class).title(),
                                answer.getArgument(0, News.class).image(),
                                answer.getArgument(0, News.class).description(),
                                answer.getArgument(0, News.class).publication(),
                                answer.getArgument(0, News.class).link(),
                                answer.getArgument(0, News.class).getFeeds()
                        ),
                        new IllegalArgumentException())))));

        StepVerifier.create(tested.scrap(SCRAPER_RETENTION_PERIOD))
                .assertNext(actual -> assertAll(
                        () -> Assertions.assertThat(actual.inserted()).isZero(),
                        () -> Assertions.assertThat(actual.errors()).isNotEmpty(),
                        () -> Assertions.assertThat(actual.errors())
                                .element(0).isInstanceOf(NewsScrapingException.class)
                                .extracting("cause").isInstanceOf(IllegalArgumentException.class)
                )).verifyComplete();

        verify(maintenancePersistencePort,
                never().description("Expect no invocation because no valid enrichment")
        ).newsLoad(anyCollection());
    }

    @Test
    void should_fail_on_persistence() {
        when(maintenancePersistencePort.newsLoad(anyCollection())).thenReturn(Mono.error(new RuntimeException("Persistence failure simulation"))
                .then(Mono.just(1)));
        when(maintenancePersistencePort.listAllNewsId()).thenReturn(Flux.empty());

        StepVerifier.create(tested.scrap(SCRAPER_RETENTION_PERIOD))
                .expectErrorSatisfies(t -> Assertions.assertThat(t)
                        .hasRootCauseInstanceOf(RuntimeException.class)
                        .hasRootCauseMessage("Persistence failure simulation"))
                .verify();

        verify(mockExchangeFunction).exchange(ArgumentMatchers.argThat(cr -> cr.url().getPath().equals("/feeds/journal_du_hacker.xml")));
        verify(mockExchangeFunction).exchange(ArgumentMatchers.argThat(cr -> cr.url().getPath().equals("/feeds/spring-blog.xml")));
        verify(rssAtomParserMock, times(2)).readEntryEvents(any(), any(ScrapedFeed.class));
    }

    @Test
    void should_fail_on_unsupported_scheme() {
        when(maintenancePersistencePort.feedList()).thenAnswer((Answer<Flux<ScrapedFeed>>) invocationOnMock -> Flux.just(
                new ScrapedFeed("0", URI.create("file://localhost/.env"), Instant.parse("2024-02-25T17:15:42Z"), null)
        ).delayElements(Duration.ofMillis(100)));  // Delay avoid Awaitility start polling after the end of scraping

        StepVerifier.create(tested.scrap(SCRAPER_RETENTION_PERIOD))
                .assertNext(next -> assertAll(
                        () -> Assertions.assertThat(next.inserted()).isZero(),
                        () -> Assertions.assertThat(next.errors()).hasSize(1)
                                .anySatisfy(s -> Assertions.assertThat(s)
                                        .isInstanceOf(FeedScrapingException.class)
                                        .hasRootCauseInstanceOf(IllegalArgumentException.class))
                )).verifyComplete();

        verify(mockExchangeFunction, never()).exchange(any());
        verify(rssAtomParserMock, never()).readEntryEvents(any(), any());
        verify(maintenancePersistencePort, never()).newsLoad(anyCollection());
    }

    @BeforeEach
    void setUp() {
        maintenancePersistencePort = mock(ScraperMaintenancePort.class);
        when(maintenancePersistencePort.listAllNewsId()).thenReturn(Flux.<String>empty()
                .delayElements(Duration.ofMillis(200)));  // Delay avoid Awaitility start polling after the and of scraping
        when(maintenancePersistencePort.newsLoad(anyCollection())).thenReturn(Mono.just(1));

        mockExchangeFunction = spy(new MockExchangeFunction());
        WebClient mockWebClient = WebClient.builder().exchangeFunction(mockExchangeFunction).build();

        URI springUri = URI.create("https://www.jedi.com/feeds/spring-blog.xml");
        URI jdhUri = URI.create("https://www.jedi.com/feeds/journal_du_hacker.xml");
        String springSha3 = Hasher.identify(springUri);
        String jdhSha3 = Hasher.identify(jdhUri);

        rssAtomParserMock = spy(new RssAtomParser() {
            @Override
            public Predicate<XMLEvent> firstItemEvent() {
                return e -> true;
            }

            @Override
            public Predicate<XMLEvent> itemEndEvent() {
                return e -> false;
            }

            @Override
            public AtomFeed readFeedProperties(List<XMLEvent> events) {
                return AtomFeed.builder()
                        .id("https://spring.io/blog.atom")
                        .title("Spring")
                        .updated(Instant.EPOCH.plus(Duration.ofDays(1)))
                        .build();
            }

            @Override
            public Mono<RawNews> readEntryEvents(List<XMLEvent> events, ScrapedFeed feed) {
                if (feed.id().equals(jdhSha3)) {
                    return Mono.just(RawNews.builder()
                            .id(Hasher.sha3("01"))
                            .link(URI.create("https://practicalprogramming.fr/dbaas-la-base-de-donnees-dans-le-cloud/"))
                            .title("Dummy Title")
                            .publication(Instant.parse("2022-04-29T12:35:41Z"))
                            .build());
                } else {
                    return Mono.just(RawNews.builder()
                            .id(Hasher.sha3("02"))
                            .link(URI.create("https://practicalprogramming.fr/dbaas-la-base-de-donnees-dans-le-cloud/"))
                            .image(URI.create("https://practicalprogramming.fr/image.jpg"))
                            .title("Dummy Title 2")
                            .publication(Instant.parse("2022-04-30T12:42:24Z"))
                            .build());
                }
            }
        });

        when(maintenancePersistencePort.feedList()).thenAnswer((Answer<Flux<ScrapedFeed>>) invocationOnMock -> Flux.just(
                new ScrapedFeed(jdhSha3, jdhUri, Instant.EPOCH, null),
                new ScrapedFeed(springSha3, springUri, Instant.EPOCH, null)
        ));
        doAnswer(a -> Mono.just(a.getArgument(0))).when(maintenancePersistencePort).feedUpdate(anyString(), any(AtomFeed.class));

        when(mockScrapEnrichmentService.applyNewsFilters(any(News.class)))
                .thenAnswer(((Answer<Mono<Try<News>>>) answer -> Mono.just(Try.of(answer.getArgument(0, News.class)))));

        doAnswer(a -> Mono.just(a.getArgument(0))).when(mockScrapEnrichmentService).applyFeedsFilters(any(AtomFeed.class));

        tested = new FeedScraperServiceImpl(Schedulers.immediate(), maintenancePersistencePort, mockWebClient, rssAtomParserMock,
                Collections.emptyList(), Map.of(), mockScrapEnrichmentService);
        tested.setClock(Clock.fixed(Instant.parse("2022-04-30T12:35:41Z"), ZoneOffset.UTC));
    }

    public static final class MockExchangeFunction implements ExchangeFunction {
        private static final Pattern FEED_PATTERN = Pattern.compile("/feeds/.*\\.xml");
        private static final Pattern ERROR_PATTERN = Pattern.compile("/error/darth-vader.xml");

        @Override
        public @NotNull Mono<ClientResponse> exchange(@NotNull ClientRequest request) {
            if (FEED_PATTERN.matcher(request.url().getPath()).matches()) {
                return Mono.just(ClientResponse.create(HttpStatus.OK)
                        .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_ATOM_XML_VALUE)
                        .body(DataBufferUtils.readInputStream(
                                () -> FeedScraperServiceTest.class.getResourceAsStream(request.url().getPath().replaceFirst("/", "")),
                                new DefaultDataBufferFactory(), 512)
                        ).build());
            } else if (ERROR_PATTERN.matcher(request.url().getPath()).matches()) {
                return Mono.just(ClientResponse.create(HttpStatus.NOT_FOUND).build());
            }

            return Mono.just(ClientResponse.create(HttpStatus.INTERNAL_SERVER_ERROR).build());
        }
    }
}