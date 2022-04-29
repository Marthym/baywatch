package fr.ght1pc9kc.baywatch.scrapper.domain;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import fr.ght1pc9kc.baywatch.common.domain.Hasher;
import fr.ght1pc9kc.baywatch.scrapper.api.RssAtomParser;
import fr.ght1pc9kc.baywatch.scrapper.infra.config.ScrapperProperties;
import fr.ght1pc9kc.baywatch.security.api.AuthenticationFacade;
import fr.ght1pc9kc.baywatch.techwatch.api.model.Feed;
import fr.ght1pc9kc.baywatch.techwatch.api.model.News;
import fr.ght1pc9kc.baywatch.techwatch.api.model.RawFeed;
import fr.ght1pc9kc.baywatch.techwatch.api.model.RawNews;
import fr.ght1pc9kc.baywatch.techwatch.api.model.State;
import fr.ght1pc9kc.baywatch.techwatch.domain.ports.FeedPersistencePort;
import fr.ght1pc9kc.baywatch.techwatch.domain.ports.NewsPersistencePort;
import fr.ght1pc9kc.scraphead.core.HeadScraper;
import fr.ght1pc9kc.scraphead.core.model.Metas;
import fr.ght1pc9kc.scraphead.core.model.opengraph.OpenGraph;
import org.assertj.core.api.Assertions;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
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

    private FeedScrapperService tested;

    private HeadScraper headScraperMock;
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

    @Test
    void should_scrap_with_opengraph() {
        when(headScraperMock.scrap(any(URI.class))).thenReturn(Mono.just(Metas.builder()
                .og(OpenGraph.builder()
                        .image(URI.create("http://www.ght1pc9kc.fr/featured.jpg"))
                        .title("OpenGraph Title")
                        .build())
                .build()
        ));
        //noinspection unchecked
        ArgumentCaptor<List<News>> captor = ArgumentCaptor.forClass(List.class);
        when(newsPersistenceMock.persist(captor.capture())).thenReturn(Mono.just(1));

        tested.startScrapping();
        Awaitility.await("for scrapping begin").timeout(Duration.ofSeconds(5))
                .pollDelay(Duration.ZERO)
                .until(() -> tested.isScrapping());
        tested.shutdownScrapping();

        News actual = captor.getValue().stream()
                .filter(n -> "a815eff60162e6f8dd18c2397ab15d0843a23a2be13bf521e517e38eb835ab28".equals(n.getId()))
                .findAny().orElseThrow();
        Assertions.assertThat(actual).isEqualTo(News.builder()
                .raw(RawNews.builder()
                        .id("a815eff60162e6f8dd18c2397ab15d0843a23a2be13bf521e517e38eb835ab28")
                        .title("OpenGraph Title")
                        .link(URI.create("https://practicalprogramming.fr/dbaas-la-base-de-donnees-dans-le-cloud/"))
                        .image(URI.create("http://www.ght1pc9kc.fr/featured.jpg"))
                        .publication(Instant.parse("2022-04-29T12:35:41Z"))
                        .build())
                .state(State.NONE)
                .build());
    }

    @Test
    void should_ignore_opengraph_image_as_data() {
        when(headScraperMock.scrapOpenGraph(any(URI.class))).thenReturn(Mono.just(
                OpenGraph.builder()
                        .image(URI.create("data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAABeAAAAQ4CA"))
                        .build()
        ));
        //noinspection unchecked
        ArgumentCaptor<List<News>> captor = ArgumentCaptor.forClass(List.class);
        when(newsPersistenceMock.persist(captor.capture())).thenReturn(Mono.just(1));

        tested.startScrapping();
        Awaitility.await("for scrapping begin").timeout(Duration.ofSeconds(5))
                .pollDelay(Duration.ZERO)
                .until(() -> tested.isScrapping());
        tested.shutdownScrapping();

        News actual = captor.getValue().stream()
                .filter(n -> "1d665bbff973032c28c72064f2073a85f8777b6ca3c3e0f9b9c2385cd2b206c0".equals(n.getId()))
                .findAny().orElseThrow();
        Assertions.assertThat(actual).isEqualTo(News.builder()
                .raw(RawNews.builder()
                        .id("1d665bbff973032c28c72064f2073a85f8777b6ca3c3e0f9b9c2385cd2b206c0")
                        .title("Dummy Title 2")
                        .link(URI.create("https://practicalprogramming.fr/dbaas-la-base-de-donnees-dans-le-cloud/"))
                        .image(URI.create("https://practicalprogramming.fr/image.jpg"))
                        .publication(Instant.parse("2022-04-30T12:42:24Z"))
                        .build())
                .state(State.NONE)
                .build());
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
            public Mono<News> readEntryEvents(List<XMLEvent> events, Feed feed) {
                if (feed.getName().equals("Reddit")) {
                    return Mono.just(
                            News.builder()
                                    .raw(RawNews.builder()
                                            .id(Hasher.sha3(feed.getName() + "01"))
                                            .link(URI.create("https://practicalprogramming.fr/dbaas-la-base-de-donnees-dans-le-cloud/"))
                                            .title("Dummy Title")
                                            .publication(Instant.parse("2022-04-29T12:35:41Z"))
                                            .build())
                                    .state(State.NONE)
                                    .build());
                } else {
                    return Mono.just(
                            News.builder()
                                    .raw(RawNews.builder()
                                            .id(Hasher.sha3(feed.getName() + "02"))
                                            .link(URI.create("https://practicalprogramming.fr/dbaas-la-base-de-donnees-dans-le-cloud/"))
                                            .image(URI.create("https://practicalprogramming.fr/image.jpg"))
                                            .title("Dummy Title 2")
                                            .publication(Instant.parse("2022-04-30T12:42:24Z"))
                                            .build())
                                    .state(State.NONE)
                                    .build()
                    );
                }
            }
        });

        URI springUri = URI.create("http://localhost:" + mockServer.port() + "/feeds/spring-blog.xml");
        URI jdhUri = URI.create("http://localhost:" + mockServer.port() + "/feeds/journal_du_hacker.xml");
        String springSha3 = Hasher.identify(springUri);
        String jdhSha3 = Hasher.identify(jdhUri);

        headScraperMock = mock(HeadScraper.class);
        when(headScraperMock.scrap(any(URI.class))).thenReturn(Mono.empty());

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
                feedPersistenceMock, newsPersistenceMock, authFacadeMock, rssAtomParserMock, headScraperMock,
                Collections.emptyList(), Map.of());
        tested.setClock(Clock.fixed(Instant.parse("2022-04-30T12:35:41Z"), ZoneOffset.UTC));
    }
}