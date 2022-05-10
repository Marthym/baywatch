package fr.ght1pc9kc.baywatch.scrapper.domain;

import fr.ght1pc9kc.baywatch.common.domain.Hasher;
import fr.ght1pc9kc.baywatch.scrapper.api.NewsFilter;
import fr.ght1pc9kc.baywatch.scrapper.api.RssAtomParser;
import fr.ght1pc9kc.baywatch.scrapper.domain.model.ScraperConfig;
import fr.ght1pc9kc.baywatch.techwatch.api.SystemMaintenanceService;
import fr.ght1pc9kc.baywatch.techwatch.api.model.Feed;
import fr.ght1pc9kc.baywatch.techwatch.api.model.RawFeed;
import fr.ght1pc9kc.baywatch.techwatch.api.model.RawNews;
import org.awaitility.Awaitility;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.stubbing.Answer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.ExchangeFunction;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class FeedScrapperServiceTest {

    static final ScraperConfig SCRAPER_PROPERTIES = new ScraperConfig(
            Duration.ofDays(1), Period.ofDays(30)
    );

    @SuppressWarnings({"Convert2Lambda", "Anonymous2MethodRef"})
    private final NewsFilter mockNewsFilter = spy(new NewsFilter() {
        @Override
        public Mono<RawNews> filter(RawNews news) {
            return Mono.just(news);
        }
    });

    private FeedScrapperService tested;
    private RssAtomParser rssAtomParserMock;
    private SystemMaintenanceService systemMaintenanceMock;
    private ExchangeFunction mockExchangeFunction;

    @Test
    void should_start_multi_scrapper() {
        tested.startScrapping();
        Awaitility.await("for scrapping begin").timeout(Duration.ofSeconds(5))
                .pollDelay(Duration.ZERO)
                .until(() -> tested.isScraping());
        tested.shutdownScrapping();

        verify(mockExchangeFunction).exchange(ArgumentMatchers.argThat(cr -> cr.url().getPath().equals("/feeds/journal_du_hacker.xml")));
        verify(mockExchangeFunction).exchange(ArgumentMatchers.argThat(cr -> cr.url().getPath().equals("/feeds/spring-blog.xml")));

        verify(rssAtomParserMock, times(2)).readEntryEvents(any(), any(RawFeed.class));
        verify(systemMaintenanceMock,
                times(1).description("Expect only one call because of the buffer to 100")
        ).newsLoad(anyCollection());
        verify(mockNewsFilter, times(2)).filter(any());
    }

    @Test
    void should_fail_scraper_without_fail_scraping() {
        URI darthVaderUri = URI.create("https://www.jedi.com/error/darth-vader.xml");
        URI springUri = URI.create("https://www.jedi.com/feeds/spring-blog.xml");
        String darthVaderSha3 = Hasher.identify(darthVaderUri);
        String springSha3 = Hasher.identify(springUri);

        when(systemMaintenanceMock.feedList()).thenReturn(Flux.just(
                RawFeed.builder()
                        .id(darthVaderSha3)
                        .name("fail")
                        .url(darthVaderUri)
                        .build(),
                RawFeed.builder()
                        .id(springSha3)
                        .name("Spring")
                        .url(springUri)
                        .build()
        ));

        tested.startScrapping();
        Awaitility.await("for scrapping begin").timeout(Duration.ofSeconds(5))
                .pollDelay(Duration.ZERO)
                .until(() -> tested.isScraping());
        tested.shutdownScrapping();

        verify(mockExchangeFunction).exchange(ArgumentMatchers.argThat(cr -> cr.url().getPath().equals("/error/darth-vader.xml")));
        verify(mockExchangeFunction).exchange(ArgumentMatchers.argThat(cr -> cr.url().getPath().equals("/feeds/spring-blog.xml")));

        verify(rssAtomParserMock, times(1)).readEntryEvents(any(), any(RawFeed.class));
        verify(systemMaintenanceMock,
                times(1).description("Expect only one call because of the buffer of 100")
        ).newsLoad(anyCollection());
    }

    @Test
    void should_fail_on_persistence() {
        when(systemMaintenanceMock.newsLoad(anyCollection())).thenReturn(Mono.error(new RuntimeException("Persistence failure simulation"))
                .then(Mono.just(1)));
        when(systemMaintenanceMock.newsList(any())).thenReturn(Flux.empty());

        tested.startScrapping();
        Awaitility.await("for scrapping begin").timeout(Duration.ofSeconds(5))
                .pollDelay(Duration.ZERO)
                .until(() -> tested.isScraping());
        tested.shutdownScrapping();

        verify(mockExchangeFunction).exchange(ArgumentMatchers.argThat(cr -> cr.url().getPath().equals("/feeds/journal_du_hacker.xml")));
        verify(mockExchangeFunction).exchange(ArgumentMatchers.argThat(cr -> cr.url().getPath().equals("/feeds/spring-blog.xml")));
        verify(rssAtomParserMock, times(2)).readEntryEvents(any(), any(RawFeed.class));
    }

    @Test
    void should_fail_on_unsupported_scheme() {
        when(systemMaintenanceMock.feedList()).thenAnswer((Answer<Flux<Feed>>) invocationOnMock -> Flux.just(
                Feed.builder().raw(RawFeed.builder()
                        .id("0")
                        .name("Unsupported")
                        .url(URI.create("file://localhost/.env"))
                        .build()).name("Reddit").build()
        ).delayElements(Duration.ofMillis(100)));  // Delay avoid Awaitility start polling after the and of scraping

        tested.startScrapping();
        Awaitility.await("for scraping begin").timeout(Duration.ofSeconds(5))
                .pollDelay(Duration.ZERO)
                .until(() -> tested.isScraping());
        tested.shutdownScrapping();

        verify(mockExchangeFunction, never()).exchange(any());
        verify(rssAtomParserMock, never()).readEntryEvents(any(), any());
        verify(systemMaintenanceMock, never()).newsLoad(anyCollection());
    }

    @BeforeEach
    void setUp() {
        systemMaintenanceMock = mock(SystemMaintenanceService.class);
        when(systemMaintenanceMock.newsList(any())).thenReturn(Flux.<RawNews>empty()
                .delayElements(Duration.ofMillis(200)));  // Delay avoid Awaitility start polling after the and of scraping
        when(systemMaintenanceMock.newsLoad(anyCollection())).thenReturn(Mono.just(1));

        mockExchangeFunction = spy(new MockExchangeFunction());
        WebClient mockWebClient = WebClient.builder().exchangeFunction(mockExchangeFunction).build();

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
            public Mono<RawNews> readEntryEvents(List<XMLEvent> events, RawFeed feed) {
                if (feed.getName().equals("Reddit")) {
                    return Mono.just(RawNews.builder()
                            .id(Hasher.sha3(feed.getName() + "01"))
                            .link(URI.create("https://practicalprogramming.fr/dbaas-la-base-de-donnees-dans-le-cloud/"))
                            .title("Dummy Title")
                            .publication(Instant.parse("2022-04-29T12:35:41Z"))
                            .build());
                } else {
                    return Mono.just(RawNews.builder()
                            .id(Hasher.sha3(feed.getName() + "02"))
                            .link(URI.create("https://practicalprogramming.fr/dbaas-la-base-de-donnees-dans-le-cloud/"))
                            .image(URI.create("https://practicalprogramming.fr/image.jpg"))
                            .title("Dummy Title 2")
                            .publication(Instant.parse("2022-04-30T12:42:24Z"))
                            .build());
                }
            }
        });

        URI springUri = URI.create("https://www.jedi.com/feeds/spring-blog.xml");
        URI jdhUri = URI.create("https://www.jedi.com/feeds/journal_du_hacker.xml");
        String springSha3 = Hasher.identify(springUri);
        String jdhSha3 = Hasher.identify(jdhUri);

        when(systemMaintenanceMock.feedList()).thenAnswer((Answer<Flux<RawFeed>>) invocationOnMock -> Flux.just(
                RawFeed.builder()
                        .id(jdhSha3)
                        .name("Reddit")
                        .url(jdhUri)
                        .build(),
                RawFeed.builder()
                        .id(springSha3)
                        .name("Spring")
                        .url(springUri)
                        .build()
        ));

        tested = new FeedScrapperService(SCRAPER_PROPERTIES,
                systemMaintenanceMock, mockWebClient, rssAtomParserMock,
                Collections.emptyList(), Map.of(), List.of(mockNewsFilter));
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
                        .body("""
                                <?xml version="1.0" encoding="UTF-8" ?>
                                <entry>Obiwan Kenobi</entry>
                                """)
                        .build());
            } else if (ERROR_PATTERN.matcher(request.url().getPath()).matches()) {
                return Mono.just(ClientResponse.create(HttpStatus.NOT_FOUND).build());
            }

            return Mono.just(ClientResponse.create(HttpStatus.INTERNAL_SERVER_ERROR).build());
        }
    }
}