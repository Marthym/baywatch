package fr.ght1pc9kc.baywatch.scraper.infra;

import com.samskivert.mustache.Mustache;
import com.sun.net.httpserver.HttpServer;
import fr.ght1pc9kc.baywatch.common.api.model.Entity;
import fr.ght1pc9kc.baywatch.common.domain.Hasher;
import fr.ght1pc9kc.baywatch.notify.api.NotifyService;
import fr.ght1pc9kc.baywatch.scraper.api.FeedScraperService;
import fr.ght1pc9kc.baywatch.scraper.domain.FeedScraperServiceImpl;
import fr.ght1pc9kc.baywatch.scraper.domain.RssAtomParserImpl;
import fr.ght1pc9kc.baywatch.scraper.domain.ScrapEnrichmentServiceImpl;
import fr.ght1pc9kc.baywatch.scraper.domain.filters.OpenGraphFilter;
import fr.ght1pc9kc.baywatch.scraper.domain.filters.SanitizerFilter;
import fr.ght1pc9kc.baywatch.scraper.domain.model.ScrapedFeed;
import fr.ght1pc9kc.baywatch.scraper.domain.ports.NewsMaintenancePort;
import fr.ght1pc9kc.baywatch.scraper.infra.config.ScraperApplicationProperties;
import fr.ght1pc9kc.baywatch.scraper.infra.config.ScraperConfiguration;
import fr.ght1pc9kc.baywatch.scraper.infra.config.WebClientConfiguration;
import fr.ght1pc9kc.baywatch.scraper.infra.controllers.ScraperTaskScheduler;
import fr.ght1pc9kc.baywatch.security.api.AuthenticationFacade;
import fr.ght1pc9kc.baywatch.security.api.model.RoleUtils;
import fr.ght1pc9kc.baywatch.security.api.model.User;
import fr.ght1pc9kc.baywatch.techwatch.api.SystemMaintenanceService;
import fr.ght1pc9kc.baywatch.techwatch.api.model.News;
import fr.ght1pc9kc.baywatch.tests.samples.UserSamples;
import fr.ght1pc9kc.scraphead.core.HeadScrapers;
import fr.ght1pc9kc.scraphead.netty.http.NettyScrapClient;
import org.assertj.core.api.Assertions;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.ArgumentCaptor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;
import org.springframework.util.StreamUtils;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;
import reactor.util.context.Context;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.Period;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@SuppressWarnings("unchecked")
class FeedScraperIntegrationTest {
    private static HttpServer server;

    private ScraperTaskScheduler tested;
    private final NewsMaintenancePort mockMaintenancePort = mock(NewsMaintenancePort.class);

    @BeforeAll
    static void beforeAll() throws IOException {
        server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0)
                .createContext("/", exchange -> {
                    try {
                        URI uri = exchange.getRequestURI();
                        String mediaType = MediaType.APPLICATION_ATOM_XML_VALUE;
                        if (uri.toString().startsWith("/entries")) {
                            mediaType = MediaType.TEXT_HTML_VALUE;
                        }
                        URL resource = FeedScraperIntegrationTest.class.getResource(uri.toString().substring(1));
                        if (resource == null) {
                            throw new NoSuchFileException(uri.toString());
                        }
                        InetSocketAddress address = exchange.getHttpContext().getServer().getAddress();
                        String body = Files.readString(Paths.get(resource.toURI()));

                        Function<String, String> templatize = t -> Mustache.compiler().compile(t)
                                .execute(Map.of(
                                        "hostname", address.getHostName(),
                                        "port", address.getPort(),
                                        "datetime", DateTimeFormatter.ISO_OFFSET_DATE_TIME.format(ZonedDateTime.now())
                                ));

                        String status = body.substring(0, 3);
                        switch (status) {
                            case "301" -> {
                                exchange.getResponseHeaders().add(HttpHeaders.LOCATION, templatize.apply(body.substring(4)));
                                exchange.sendResponseHeaders(HttpStatus.MOVED_PERMANENTLY.value(), 0);
                                try (OutputStream responseBody = exchange.getResponseBody()) {
                                    StreamUtils.copy(HttpStatus.MOVED_PERMANENTLY.getReasonPhrase(), StandardCharsets.UTF_8, responseBody);
                                    responseBody.flush();
                                }
                            }
                            case "418" -> exchange.sendResponseHeaders(HttpStatus.INTERNAL_SERVER_ERROR.value(), 0);
                            case "500" -> {
                                exchange.sendResponseHeaders(HttpStatus.INTERNAL_SERVER_ERROR.value(), 0);
                                try (OutputStream responseBody = exchange.getResponseBody()) {
                                    responseBody.flush();
                                }
                            }
                            default -> {
                                try (OutputStream responseBody = exchange.getResponseBody()) {
                                    exchange.getResponseHeaders().set(HttpHeaders.CONTENT_TYPE, mediaType);
                                    exchange.sendResponseHeaders(HttpStatus.OK.value(), 0);
                                    StreamUtils.copy(templatize.apply(body), StandardCharsets.UTF_8, responseBody);
                                    responseBody.flush();
                                }
                            }
                        }
                    } catch (Exception e) {
                        exchange.sendResponseHeaders(HttpStatus.INTERNAL_SERVER_ERROR.value(), 0);
                        try (OutputStream responseBody = exchange.getResponseBody()) {
                            StreamUtils.copy(HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase() + "\n" + e.getLocalizedMessage(), StandardCharsets.UTF_8, responseBody);
                            responseBody.flush();
                        }
                    }
                }).getServer();

        server.start();
    }

    @BeforeEach
    void setUp() {
        ScraperConfiguration scraperConfiguration = new ScraperConfiguration();
        ScraperApplicationProperties scraperProperties = new ScraperApplicationProperties(
                true, Duration.ofDays(1), Period.ofDays(1), Duration.ofSeconds(1), new ScraperApplicationProperties.DnsProperties(Duration.ofSeconds(2)));
        WebClientConfiguration webClientConfiguration = new WebClientConfiguration();
        HttpClient nettyHttpClient = webClientConfiguration.getNettyHttpClient(scraperProperties);
        WebClient webClientMock = webClientConfiguration.getScraperWebClient(nettyHttpClient);

        when(mockMaintenancePort.listAllNewsId()).thenReturn(Flux.empty());
        FeedScraperService scraperService = new FeedScraperServiceImpl(
                scraperConfiguration.getScraperScheduler(),
                mockMaintenancePort,
                webClientMock,
                new RssAtomParserImpl(),
                Collections.emptyList(),
                Map.of(),
                new ScrapEnrichmentServiceImpl(
                        List.of(
                                new OpenGraphFilter(HeadScrapers.builder(new NettyScrapClient(nettyHttpClient)).build()),
                                new SanitizerFilter()),
                        List.of(new SanitizerFilter()),
                        mockFacadeFor(UserSamples.YODA), mock(SystemMaintenanceService.class), mock(NotifyService.class),
                        scraperConfiguration.getScraperScheduler())
        );

        tested = new ScraperTaskScheduler(scraperService, scraperProperties);
    }

    @ParameterizedTest
    @CsvSource({
            "feeds/132-feed-canonical.xml, entries/132-blog_devgenius_io",
            "feeds/132-http-server-incomplete.xml, entries/132-blog_devgenius_io",
            "feeds/132-http-server-incomplete-teapot.xml, ",
            "feeds/135-invalid-feed-flux.xml, ",
    })
    void should_scrap_feeds(String feed, String expected) {
        URI BASE_URI = URI.create(String.format("http://%s:%d/", server.getAddress().getHostName(), server.getAddress().getPort()));
        when(mockMaintenancePort.feedList()).thenReturn(
                Flux.just(new ScrapedFeed("1", BASE_URI.resolve(feed)))
        );
        ArgumentCaptor<Collection<News>> loadCaptor = ArgumentCaptor.forClass(Collection.class);
        when(mockMaintenancePort.newsLoad(loadCaptor.capture()))
                .thenAnswer(a -> Mono.just(a.getArgument(0, Collection.class).size()));

        tested.run();

        Awaitility.await().atMost(Duration.ofSeconds(10)).until(() -> !tested.isScraping());

        List<News> actual = loadCaptor.getAllValues().stream()
                .flatMap(Collection::stream)
                .collect(Collectors.toList());

        if (expected != null) {
            Assertions.assertThat(actual)
                    .extracting(News::id)
                    .containsOnly(Hasher.identify(URI.create(BASE_URI + expected)));
        } else {
            Assertions.assertThat(actual).isEmpty();
        }
    }

    @SuppressWarnings("SameParameterValue")
    private static AuthenticationFacade mockFacadeFor(Entity<User> user) {
        return new AuthenticationFacade() {
            @Override
            public Mono<Entity<User>> getConnectedUser() {
                return Mono.just(user);
            }

            @Override
            public Context withAuthentication(Entity<User> user) {
                Authentication authentication = new PreAuthenticatedAuthenticationToken(user, null,
                        AuthorityUtils.createAuthorityList(user.self().roles.stream().map(RoleUtils::toSpringAuthority).toArray(String[]::new)));
                return ReactiveSecurityContextHolder.withAuthentication(authentication);
            }
        };
    }
}
