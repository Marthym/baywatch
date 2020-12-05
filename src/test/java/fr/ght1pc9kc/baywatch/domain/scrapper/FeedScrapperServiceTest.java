package fr.ght1pc9kc.baywatch.domain.scrapper;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.machinezoo.noexception.Exceptions;
import fr.ght1pc9kc.baywatch.api.FeedPersistencePort;
import fr.ght1pc9kc.baywatch.api.NewsPersistencePort;
import fr.ght1pc9kc.baywatch.api.RssAtomParser;
import fr.ght1pc9kc.baywatch.api.model.Feed;
import fr.ght1pc9kc.baywatch.api.model.News;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import wiremock.org.apache.commons.io.IOUtils;

import java.io.InputStream;
import java.net.URI;
import java.time.Clock;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class FeedScrapperServiceTest {

    static WireMockServer mockServer = new WireMockServer(WireMockConfiguration.wireMockConfig().dynamicPort());

    private FeedScrapperService tested;

    private NewsPersistencePort newsPersistenceMock;
    private RssAtomParser rssAtomParserMock;
    private FeedPersistencePort feedPersistenceMock;

    @Test
    void should_start_multi_scrapper() {
        tested.startScrapping();
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
        when(feedPersistenceMock.list()).thenReturn(Flux.just(
                Feed.builder()
                        .id(42)
                        .name("fail")
                        .url(URI.create("http://localhost:" + mockServer.port() + "/error/dark-vader.xml"))
                        .build(),
                Feed.builder()
                        .id(24)
                        .name("Spring")
                        .url(URI.create("http://localhost:" + mockServer.port() + "/feeds/spring-blog.xml"))
                        .build()
        ));

        tested.startScrapping();
        tested.shutdownScrapping();

        mockServer.verify(WireMock.getRequestedFor(WireMock.urlEqualTo("/error/dark-vader.xml")));
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

        tested.startScrapping();
        tested.shutdownScrapping();

        mockServer.verify(WireMock.getRequestedFor(WireMock.urlEqualTo("/error/dark-vader.xml")));
        mockServer.verify(WireMock.getRequestedFor(WireMock.urlEqualTo("/feeds/spring-blog.xml")));
        verify(rssAtomParserMock, times(2)).parse(any(Feed.class), any());
    }

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
                WireMock.get(WireMock.urlEqualTo("/error/dark-vader.xml"))
                        .willReturn(WireMock.aResponse()
                                .withStatus(HttpStatus.NOT_FOUND.value())));

        mockServer.start();
    }

    @BeforeEach
    void setUp() {
        newsPersistenceMock = mock(NewsPersistencePort.class);
        when(newsPersistenceMock.persist(anyCollection())).thenReturn(Mono.just("").then());

        rssAtomParserMock = spy(new RssAtomParser() {
            @Override
            public Flux<News> parse(Feed feed, InputStream is) {
                // Must consume the inputstream
                Exceptions.wrap().get(() -> IOUtils.toByteArray(is));
                return Flux.just(News.builder()
                        .id(UUID.randomUUID().toString())
                        .link(URI.create("https://practicalprogramming.fr/dbaas-la-base-de-donnees-dans-le-cloud/"))
                        .build());
            }
        });

        feedPersistenceMock = mock(FeedPersistencePort.class);
        when(feedPersistenceMock.list()).thenReturn(Flux.just(
                Feed.builder()
                        .id(42)
                        .name("Reddit")
                        .url(URI.create("http://localhost:" + mockServer.port() + "/feeds/journal_du_hacker.xml"))
                        .build(),
                Feed.builder()
                        .id(24)
                        .name("Spring")
                        .url(URI.create("http://localhost:" + mockServer.port() + "/feeds/spring-blog.xml"))
                        .build()
        ));

        tested = new FeedScrapperService(Clock.systemUTC(),
                feedPersistenceMock, newsPersistenceMock, rssAtomParserMock);
    }

    @AfterAll
    static void afterAll() {
        mockServer.stop();
    }
}