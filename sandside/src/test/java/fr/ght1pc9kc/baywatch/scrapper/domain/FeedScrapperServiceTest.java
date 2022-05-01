package fr.ght1pc9kc.baywatch.scrapper.domain;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import fr.ght1pc9kc.baywatch.common.domain.Hasher;
import fr.ght1pc9kc.baywatch.scrapper.api.NewsFilter;
import fr.ght1pc9kc.baywatch.scrapper.api.RssAtomParser;
import fr.ght1pc9kc.baywatch.scrapper.infra.config.ScrapperProperties;
import fr.ght1pc9kc.baywatch.security.api.AuthenticationFacade;
import fr.ght1pc9kc.baywatch.techwatch.api.model.Feed;
import fr.ght1pc9kc.baywatch.techwatch.api.model.RawFeed;
import fr.ght1pc9kc.baywatch.techwatch.api.model.RawNews;
import fr.ght1pc9kc.baywatch.techwatch.domain.ports.FeedPersistencePort;
import fr.ght1pc9kc.baywatch.techwatch.domain.ports.NewsPersistencePort;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.stubbing.Answer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.context.Context;

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
import java.util.Set;
import java.util.function.Predicate;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class FeedScrapperServiceTest {

    static final WireMockServer mockServer = new WireMockServer(WireMockConfiguration.wireMockConfig().dynamicPort());
    static final ScrapperProperties scraperProperties = new ScrapperProperties(
            true, Duration.ofDays(1), Duration.ofSeconds(2), Period.ofDays(30), Set.of("http", "https")
    );

    @SuppressWarnings({"Convert2Lambda", "Anonymous2MethodRef"})
    private static final NewsFilter mockNewsFilter = spy(new NewsFilter() {
        @Override
        public Mono<RawNews> filter(RawNews news) {
            return Mono.just(news);
        }
    });

    private FeedScrapperService tested;
    private NewsPersistencePort newsPersistenceMock;
    private RssAtomParser rssAtomParserMock;
    private FeedPersistencePort feedPersistenceMock;

    @BeforeAll
    static void stubAllMockServerRoute() {
        mockServer.stubFor(
                WireMock.get(WireMock.urlMatching("/feeds/.*\\.xml"))
                        .willReturn(WireMock.aResponse()
                                .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_ATOM_XML_VALUE)
                                .withStatus(HttpStatus.OK.value())
                                .withBody("""
                                        <?xml version="1.0" encoding="UTF-8" ?>
                                        <entry>Obiwan Kenobi</entry>
                                        """))
        );
        mockServer.stubFor(
                WireMock.get(WireMock.urlEqualTo("/error/darth-vader.xml"))
                        .willReturn(WireMock.aResponse()
                                .withStatus(HttpStatus.NOT_FOUND.value())));

        mockServer.start();
    }

    @AfterAll
    static void afterAll() {
        mockServer.stop();
    }

    @Test
    void should_start_multi_scrapper() {
        tested.startScrapping();
        Awaitility.await("for scrapping begin").timeout(Duration.ofSeconds(5))
                .pollDelay(Duration.ZERO)
                .until(() -> tested.isScrapping());
        tested.shutdownScrapping();

        mockServer.verify(WireMock.getRequestedFor(WireMock.urlEqualTo("/feeds/journal_du_hacker.xml")));
        mockServer.verify(WireMock.getRequestedFor(WireMock.urlEqualTo("/feeds/spring-blog.xml")));

        verify(rssAtomParserMock, times(2)).readEntryEvents(any(), any(Feed.class));
        verify(newsPersistenceMock,
                times(1).description("Expect only one call because of the buffer to 100")
        ).persist(anyCollection());
        verify(mockNewsFilter, times(2)).filter(any());
    }

    @Test
    void should_fail_scrapper_without_fail_scrapping() {
        URI darthVaderUri = URI.create("http://localhost:" + mockServer.port() + "/error/darth-vader.xml");
        URI springUri = URI.create("http://localhost:" + mockServer.port() + "/feeds/spring-blog.xml");
        String darthVaderSha3 = Hasher.identify(darthVaderUri);
        String springSha3 = Hasher.identify(springUri);

        when(feedPersistenceMock.list()).thenReturn(Flux.just(
                Feed.builder().raw(RawFeed.builder()
                        .id(darthVaderSha3)
                        .name("fail")
                        .url(darthVaderUri)
                        .build()).name("fail").build(),
                Feed.builder().raw(RawFeed.builder()
                        .id(springSha3)
                        .name("Spring")
                        .url(springUri)
                        .build()).name("Spring").build()
        ));

        tested.startScrapping();
        Awaitility.await("for scrapping begin").timeout(Duration.ofSeconds(5))
                .pollDelay(Duration.ZERO)
                .until(() -> tested.isScrapping());
        tested.shutdownScrapping();

        mockServer.verify(WireMock.getRequestedFor(WireMock.urlEqualTo("/error/darth-vader.xml")));
        mockServer.verify(WireMock.getRequestedFor(WireMock.urlEqualTo("/feeds/spring-blog.xml")));
        verify(rssAtomParserMock, times(1)).readEntryEvents(any(), any(Feed.class));
        verify(newsPersistenceMock,
                times(1).description("Expect only one call because of the buffer of 100")
        ).persist(anyCollection());
    }

    @Test
    void should_fail_on_persistence() {
        reset(newsPersistenceMock);
        when(newsPersistenceMock.persist(anyCollection())).thenReturn(Mono.error(new RuntimeException()).then(Mono.just(1)));
        when(newsPersistenceMock.list()).thenReturn(Flux.empty());

        tested.startScrapping();
        Awaitility.await("for scrapping begin").timeout(Duration.ofSeconds(5))
                .pollDelay(Duration.ZERO)
                .until(() -> tested.isScrapping());
        tested.shutdownScrapping();

        mockServer.verify(WireMock.getRequestedFor(WireMock.urlEqualTo("/error/darth-vader.xml")));
        mockServer.verify(WireMock.getRequestedFor(WireMock.urlEqualTo("/feeds/spring-blog.xml")));
        verify(rssAtomParserMock, times(2)).readEntryEvents(any(), any(Feed.class));
    }

    @BeforeEach
    void setUp() {
        newsPersistenceMock = mock(NewsPersistencePort.class);
        when(newsPersistenceMock.list()).thenReturn(Flux.empty());
        when(newsPersistenceMock.persist(anyCollection())).thenReturn(Mono.just(1));

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
            public Mono<RawNews> readEntryEvents(List<XMLEvent> events, Feed feed) {
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

        URI springUri = URI.create("http://localhost:" + mockServer.port() + "/feeds/spring-blog.xml");
        URI jdhUri = URI.create("http://localhost:" + mockServer.port() + "/feeds/journal_du_hacker.xml");
        String springSha3 = Hasher.identify(springUri);
        String jdhSha3 = Hasher.identify(jdhUri);

        feedPersistenceMock = mock(FeedPersistencePort.class);
        when(feedPersistenceMock.list()).thenAnswer((Answer<Flux<Feed>>) invocationOnMock -> Flux.just(
                Feed.builder().raw(RawFeed.builder()
                        .id(jdhSha3)
                        .name("Reddit")
                        .url(jdhUri)
                        .build()).name("Reddit").build(),
                Feed.builder().raw(RawFeed.builder()
                        .id(springSha3)
                        .name("Spring")
                        .url(springUri)
                        .build()).name("Spring").build()
        ));

        AuthenticationFacade authFacadeMock = mock(AuthenticationFacade.class);
        when(authFacadeMock.withSystemAuthentication()).thenReturn(Context.empty());

        tested = new FeedScrapperService(scraperProperties,
                feedPersistenceMock, newsPersistenceMock, authFacadeMock, rssAtomParserMock,
                Collections.emptyList(), Map.of(), List.of(mockNewsFilter));
        tested.setClock(Clock.fixed(Instant.parse("2022-04-30T12:35:41Z"), ZoneOffset.UTC));
    }
}