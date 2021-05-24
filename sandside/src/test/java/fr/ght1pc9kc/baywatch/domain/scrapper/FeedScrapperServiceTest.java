package fr.ght1pc9kc.baywatch.domain.scrapper;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.machinezoo.noexception.Exceptions;
import fr.ght1pc9kc.baywatch.api.AuthenticationService;
import fr.ght1pc9kc.baywatch.api.model.*;
import fr.ght1pc9kc.baywatch.api.scrapper.RssAtomParser;
import fr.ght1pc9kc.baywatch.api.model.State;
import fr.ght1pc9kc.baywatch.domain.techwatch.ports.FeedPersistencePort;
import fr.ght1pc9kc.baywatch.domain.techwatch.ports.NewsPersistencePort;
import fr.ght1pc9kc.baywatch.domain.scrapper.opengraph.OpenGraphScrapper;
import fr.ght1pc9kc.baywatch.domain.scrapper.opengraph.model.OpenGraph;
import fr.ght1pc9kc.baywatch.domain.utils.Hasher;
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
import wiremock.org.apache.commons.io.IOUtils;

import java.io.InputStream;
import java.net.URI;
import java.time.Duration;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.Mockito.*;

class FeedScrapperServiceTest {

    static final WireMockServer mockServer = new WireMockServer(WireMockConfiguration.wireMockConfig().dynamicPort());

    private FeedScrapperService tested;

    private OpenGraphScrapper openGraphScrapper;
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
                                .withBody("Obiwan Kenobi"))
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

        verify(rssAtomParserMock, times(2)).parse(any(Feed.class), any());
        verify(newsPersistenceMock,
                times(1).description("Expect only one call because of the buffer to 100")
        ).persist(anyCollection());
    }

    @Test
    void should_fail_scrapper_without_fail_scrapping() {
        URI darthVaderUri = URI.create("http://localhost:" + mockServer.port() + "/error/darth-vader.xml");
        URI springUri = URI.create("http://localhost:" + mockServer.port() + "/feeds/spring-blog.xml");
        String darthVaderSha3 = Hasher.sha3(darthVaderUri.toString());
        String springSha3 = Hasher.sha3(springUri.toString());

        when(feedPersistenceMock.list()).thenReturn(Flux.just(
                Feed.builder().raw(RawFeed.builder()
                        .id(darthVaderSha3)
                        .name("fail")
                        .url(darthVaderUri)
                        .build()).build(),
                Feed.builder().raw(RawFeed.builder()
                        .id(springSha3)
                        .name("Spring")
                        .url(springUri)
                        .build()).build()
        ));

        tested.startScrapping();
        Awaitility.await("for scrapping begin").timeout(Duration.ofSeconds(5))
                .pollDelay(Duration.ZERO)
                .until(() -> tested.isScrapping());
        tested.shutdownScrapping();

        mockServer.verify(WireMock.getRequestedFor(WireMock.urlEqualTo("/error/darth-vader.xml")));
        mockServer.verify(WireMock.getRequestedFor(WireMock.urlEqualTo("/feeds/spring-blog.xml")));
        verify(rssAtomParserMock, times(2)).parse(any(Feed.class), any());
        verify(newsPersistenceMock,
                times(1).description("Expect only one call because of the buffer to 100")
        ).persist(anyCollection());
    }

    @Test
    void should_fail_on_persistence() {
        reset(newsPersistenceMock);
        when(newsPersistenceMock.persist(anyCollection())).thenReturn(Mono.error(new RuntimeException()).then());
        when(newsPersistenceMock.list()).thenReturn(Flux.empty());

        tested.startScrapping();
        Awaitility.await("for scrapping begin").timeout(Duration.ofSeconds(5))
                .pollDelay(Duration.ZERO)
                .until(() -> tested.isScrapping());
        tested.shutdownScrapping();

        mockServer.verify(WireMock.getRequestedFor(WireMock.urlEqualTo("/error/darth-vader.xml")));
        mockServer.verify(WireMock.getRequestedFor(WireMock.urlEqualTo("/feeds/spring-blog.xml")));
        verify(rssAtomParserMock, times(2)).parse(any(Feed.class), any());
    }

    @Test
    void should_scrap_with_opengraph() {
        when(openGraphScrapper.scrap(any(URI.class))).thenReturn(Mono.just(
                OpenGraph.builder()
                        .image(URI.create("http://www.ght1pc9kc.fr/featured.jpg"))
                        .title("OpenGraph Title")
                        .build()
        ));
        //noinspection unchecked
        ArgumentCaptor<List<News>> captor = ArgumentCaptor.forClass(List.class);
        when(newsPersistenceMock.persist(captor.capture())).thenReturn(Mono.just("").then());

        tested.startScrapping();
        Awaitility.await("for scrapping begin").timeout(Duration.ofSeconds(5))
                .pollDelay(Duration.ZERO)
                .until(() -> tested.isScrapping());
        tested.shutdownScrapping();

        News actual = captor.getValue().stream()
                .filter(n -> "5c0a1b1971fb82cf9b6049430f19e6956d1bfe98c3ede589a3656091d5414b63".equals(n.getId()))
                .findAny().orElseThrow();
        Assertions.assertThat(actual).isEqualTo(News.builder()
                .raw(RawNews.builder()
                        .id("5c0a1b1971fb82cf9b6049430f19e6956d1bfe98c3ede589a3656091d5414b63")
                        .title("OpenGraph Title")
                        .link(URI.create("https://practicalprogramming.fr/dbaas-la-base-de-donnees-dans-le-cloud/"))
                        .image(URI.create("http://www.ght1pc9kc.fr/featured.jpg"))
                        .build())
                .state(State.NONE)
                .build());
    }

    @Test
    void should_ignore_opengraph_image_as_data() {
        when(openGraphScrapper.scrap(any(URI.class))).thenReturn(Mono.just(
                OpenGraph.builder()
                        .image(URI.create("data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAABeAAAAQ4CA"))
                        .build()
        ));
        //noinspection unchecked
        ArgumentCaptor<List<News>> captor = ArgumentCaptor.forClass(List.class);
        when(newsPersistenceMock.persist(captor.capture())).thenReturn(Mono.just("").then());

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
                        .build())
                .state(State.NONE)
                .build());
    }

    @BeforeEach
    void setUp() {
        newsPersistenceMock = mock(NewsPersistencePort.class);
        when(newsPersistenceMock.list()).thenReturn(Flux.empty());
        when(newsPersistenceMock.persist(anyCollection())).thenReturn(Mono.just("").then());

        //Mockito does not support Lambda
        //noinspection Convert2Lambda
        rssAtomParserMock = spy(new RssAtomParser() {
            @Override
            public Flux<News> parse(Feed feed, InputStream is) {
                // Must consume the inputstream
                Exceptions.wrap().get(() -> IOUtils.toByteArray(is));
                return Flux.just(
                        News.builder()
                                .raw(RawNews.builder()
                                        .id(Hasher.sha3(feed.getName() + "01"))
                                        .link(URI.create("https://practicalprogramming.fr/dbaas-la-base-de-donnees-dans-le-cloud/"))
                                        .title("Dummy Title")
                                        .build())
                                .state(State.NONE)
                                .build(),
                        News.builder()
                                .raw(RawNews.builder()
                                        .id(Hasher.sha3(feed.getName() + "02"))
                                        .link(URI.create("https://practicalprogramming.fr/dbaas-la-base-de-donnees-dans-le-cloud/"))
                                        .image(URI.create("https://practicalprogramming.fr/image.jpg"))
                                        .title("Dummy Title 2")
                                        .build())
                                .state(State.NONE)
                                .build()
                );
            }
        });

        URI springUri = URI.create("http://localhost:" + mockServer.port() + "/feeds/spring-blog.xml");
        URI jdhUri = URI.create("http://localhost:" + mockServer.port() + "/feeds/journal_du_hacker.xml");
        String springSha3 = Hasher.sha3(springUri.toString());
        String jdhSha3 = Hasher.sha3(jdhUri.toString());

        openGraphScrapper = mock(OpenGraphScrapper.class);
        when(openGraphScrapper.scrap(any(URI.class))).thenReturn(Mono.empty());

        feedPersistenceMock = mock(FeedPersistencePort.class);
        when(feedPersistenceMock.list()).thenAnswer((Answer<Flux<Feed>>) invocationOnMock -> Flux.just(
                Feed.builder().raw(RawFeed.builder()
                        .id(jdhSha3)
                        .name("Reddit")
                        .url(jdhUri)
                        .build()).build(),
                Feed.builder().raw(RawFeed.builder()
                        .id(springSha3)
                        .name("Spring")
                        .url(springUri)
                        .build()).build()
        ));

        AuthenticationService authServiceMock = mock(AuthenticationService.class);
        when(authServiceMock.withSystemAuthentication()).thenReturn(Context.empty());

        tested = new FeedScrapperService(Duration.ofDays(1),
                openGraphScrapper, feedPersistenceMock, newsPersistenceMock, rssAtomParserMock,
                Collections.emptyList(), authServiceMock);
    }
}