package fr.ght1pc9kc.baywatch.scraper.domain.filters;

import fr.ght1pc9kc.baywatch.common.domain.Hasher;
import fr.ght1pc9kc.baywatch.techwatch.api.model.RawNews;
import fr.ght1pc9kc.scraphead.core.HeadScraper;
import fr.ght1pc9kc.scraphead.core.http.ScrapRequest;
import fr.ght1pc9kc.scraphead.core.model.Metas;
import fr.ght1pc9kc.scraphead.core.model.links.Links;
import fr.ght1pc9kc.scraphead.core.model.opengraph.OpenGraph;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.net.HttpCookie;
import java.net.URI;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class OpenGraphFilterTest {
    private static final RawNews RAW = RawNews.builder()
            .id("0")
            .link(URI.create("http://www.jedi.com/obiwan/kenobi"))
            .title("Start with this title")
            .description("Start with this description")
            .build();
    private OpenGraphFilter tested;
    private HeadScraper mockScraper;

    @BeforeEach
    void setUp() {
        mockScraper = mock(HeadScraper.class);
        tested = new OpenGraphFilter(mockScraper);
    }

    @Test
    void should_filter_opengraph() {
        when(mockScraper.scrap(any(ScrapRequest.class))).thenReturn(Mono.just(
                Metas.builder()
                        .resourceUrl(RAW.getLink())
                        .og(OpenGraph.builder()
                                .title("Opengraph title")
                                .description("Opengraph description")
                                .image(URI.create("https://open.graph.img/image.jpg"))
                                .build()
                        ).build()
        ));

        StepVerifier.create(tested.filter(RAW))
                .assertNext(actual -> Assertions.assertAll(
                        () -> assertThat(actual.getId()).isEqualTo("0"),
                        () -> assertThat(actual.getTitle()).isEqualTo("Opengraph title"),
                        () -> assertThat(actual.getDescription()).isEqualTo("Opengraph description"),
                        () -> assertThat(actual.getImage()).isEqualTo(URI.create("https://open.graph.img/image.jpg"))
                ))
                .verifyComplete();

        ArgumentCaptor<ScrapRequest> captor = ArgumentCaptor.forClass(ScrapRequest.class);
        verify(mockScraper).scrap(captor.capture());

        assertThat(captor.getValue()).extracting(ScrapRequest::location)
                .isEqualTo(RAW.getLink());
    }

    @Test
    void should_filter_opengraph_with_title() {
        when(mockScraper.scrap(any(ScrapRequest.class))).thenReturn(Mono.just(
                Metas.builder()
                        .resourceUrl(RAW.getLink())
                        .title("Title from title tag")
                        .og(OpenGraph.builder()
                                .title("Opengraph title")
                                .description("Opengraph description")
                                .image(URI.create("https://open.graph.img/image.jpg"))
                                .build()
                        ).build()
        ));

        StepVerifier.create(tested.filter(RAW))
                .assertNext(actual -> Assertions.assertAll(
                        () -> assertThat(actual.getId()).isEqualTo("0"),
                        () -> assertThat(actual.getTitle()).isEqualTo("Opengraph title"),
                        () -> assertThat(actual.getDescription()).isEqualTo("Opengraph description"),
                        () -> assertThat(actual.getImage()).isEqualTo(URI.create("https://open.graph.img/image.jpg"))
                ))
                .verifyComplete();

        ArgumentCaptor<ScrapRequest> captor = ArgumentCaptor.forClass(ScrapRequest.class);
        verify(mockScraper).scrap(captor.capture());

        assertThat(captor.getValue()).extracting(ScrapRequest::location)
                .isEqualTo(RAW.getLink());
    }

    @Test
    void should_filter_opengraph_with_canonical() {
        when(mockScraper.scrap(any(ScrapRequest.class))).thenReturn(Mono.just(
                Metas.builder()
                        .resourceUrl(URI.create("https://www.jedi.com/obiwan/kenobi/canonical/"))
                        .og(OpenGraph.builder()
                                .title("Opengraph title")
                                .build()
                        ).links(Links.builder()
                                .canonical(URI.create("https://www.jedi.com/obiwan/kenobi/canonical/"))
                                .build())
                        .build()
        ));

        StepVerifier.create(tested.filter(RAW))
                .assertNext(actual -> Assertions.assertAll(
                        () -> assertThat(actual.getTitle()).isEqualTo("Opengraph title"),
                        () -> assertThat(actual.getDescription()).isEqualTo("Start with this description"),
                        () -> assertThat(actual.getId()).isEqualTo("48e3dc849d49a1ea3d73e8abb3630d947a722e662160b10d05325a9d902d7696"),
                        () -> assertThat(actual.getLink()).isEqualTo(URI.create("https://www.jedi.com/obiwan/kenobi/canonical/"))
                ))
                .verifyComplete();

        ArgumentCaptor<ScrapRequest> captor = ArgumentCaptor.forClass(ScrapRequest.class);
        verify(mockScraper).scrap(captor.capture());

        assertThat(captor.getValue()).extracting(ScrapRequest::location)
                .isEqualTo(RAW.getLink());
    }

    @Test
    void should_filter_opengraph_with_redirection() {
        when(mockScraper.scrap(any(ScrapRequest.class))).thenReturn(Mono.just(
                Metas.builder()
                        .resourceUrl(URI.create("https://www.jedi.com/obiwan/kenobi/redirect/"))
                        .og(OpenGraph.builder()
                                .title("Opengraph title")
                                .build()
                        ).build()
        ));

        StepVerifier.create(tested.filter(RAW))
                .assertNext(actual -> Assertions.assertAll(
                        () -> assertThat(actual.getTitle()).isEqualTo("Opengraph title"),
                        () -> assertThat(actual.getDescription()).isEqualTo("Start with this description"),
                        () -> assertThat(actual.getId()).isEqualTo(Hasher.identify(URI.create("https://www.jedi.com/obiwan/kenobi/redirect/"))),
                        () -> assertThat(actual.getLink()).isEqualTo(URI.create("https://www.jedi.com/obiwan/kenobi/redirect/"))
                ))
                .verifyComplete();

        ArgumentCaptor<ScrapRequest> captor = ArgumentCaptor.forClass(ScrapRequest.class);
        verify(mockScraper).scrap(captor.capture());

        assertThat(captor.getValue()).extracting(ScrapRequest::location)
                .isEqualTo(RAW.getLink());
    }

    @Test
    void should_filter_opengraph_without_OG() {
        when(mockScraper.scrap(any(ScrapRequest.class))).thenReturn(Mono.just(
                Metas.builder()
                        .resourceUrl(URI.create("https://www.jedi.com/obiwan/kenobi/canonical/"))
                        .links(Links.builder()
                                .canonical(URI.create("https://www.jedi.com/obiwan/kenobi/canonical/"))
                                .build())
                        .build()
        ));

        StepVerifier.create(tested.filter(RAW))
                .assertNext(actual -> Assertions.assertAll(
                        () -> assertThat(actual.getTitle()).isEqualTo(RAW.getTitle()),
                        () -> assertThat(actual.getDescription()).isEqualTo(RAW.getDescription()),
                        () -> assertThat(actual.getId()).isEqualTo("48e3dc849d49a1ea3d73e8abb3630d947a722e662160b10d05325a9d902d7696"),
                        () -> assertThat(actual.getLink()).isEqualTo(URI.create("https://www.jedi.com/obiwan/kenobi/canonical/"))
                ))
                .verifyComplete();

        ArgumentCaptor<ScrapRequest> captor = ArgumentCaptor.forClass(ScrapRequest.class);
        verify(mockScraper).scrap(captor.capture());

        assertThat(captor.getValue()).extracting(ScrapRequest::location)
                .isEqualTo(RAW.getLink());
    }

    @Test
    void should_filter_opengraph_with_empty_OG() {
        when(mockScraper.scrap(any(ScrapRequest.class))).thenReturn(Mono.empty());

        StepVerifier.create(tested.filter(RAW))
                .assertNext(actual -> assertThat(actual).isSameAs(RAW))
                .verifyComplete();

        ArgumentCaptor<ScrapRequest> captor = ArgumentCaptor.forClass(ScrapRequest.class);
        verify(mockScraper).scrap(captor.capture());

        assertThat(captor.getValue()).extracting(ScrapRequest::location)
                .isEqualTo(RAW.getLink());
    }

    @Test
    void should_filter_opengraph_for_youtube() {
        when(mockScraper.scrap(any(ScrapRequest.class))).thenReturn(Mono.just(
                Metas.builder()
                        .resourceUrl(URI.create("http://www.youtube.com/obiwan/kenobi"))
                        .og(OpenGraph.builder()
                                .title("Opengraph title")
                                .description("Opengraph description")
                                .image(URI.create("https://open.graph.img/image.jpg"))
                                .build()
                        ).build()
        ));

        StepVerifier.create(tested.filter(RAW.withLink(URI.create("http://www.youtube.com/obiwan/kenobi"))))
                .assertNext(actual -> Assertions.assertAll(
                        () -> assertThat(actual.getId()).isEqualTo("0"),
                        () -> assertThat(actual.getTitle()).isEqualTo("Opengraph title"),
                        () -> assertThat(actual.getDescription()).isEqualTo("Opengraph description"),
                        () -> assertThat(actual.getImage()).isEqualTo(URI.create("https://open.graph.img/image.jpg"))
                ))
                .verifyComplete();

        ArgumentCaptor<ScrapRequest> captor = ArgumentCaptor.forClass(ScrapRequest.class);
        verify(mockScraper).scrap(captor.capture());

        Assertions.assertAll(
                () -> assertThat(captor.getValue()).extracting(ScrapRequest::location)
                        .isEqualTo(URI.create("http://www.youtube.com/obiwan/kenobi")),
                () -> assertThat(captor.getValue().cookies()).isNotEmpty()
                        .extracting(HttpCookie::getValue)
                        .containsExactly("YES+0")

        );
    }

    @Test
    void should_filter_opengraph_with_illegal_scheme() {
        when(mockScraper.scrap(any(ScrapRequest.class))).thenReturn(Mono.empty());

        RawNews illegalRawNews = RAW.withLink(URI.create("file://localhost/illegal"));
        StepVerifier.create(tested.filter(illegalRawNews))
                .assertNext(actual -> assertThat(actual).isSameAs(illegalRawNews))
                .verifyComplete();

        verify(mockScraper, never()).scrap(any(ScrapRequest.class));
    }
}