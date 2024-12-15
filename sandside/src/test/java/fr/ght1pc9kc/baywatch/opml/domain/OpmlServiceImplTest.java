package fr.ght1pc9kc.baywatch.opml.domain;

import fr.ght1pc9kc.baywatch.common.domain.QueryContext;
import fr.ght1pc9kc.baywatch.opml.api.OpmlService;
import fr.ght1pc9kc.baywatch.security.api.AuthenticationFacade;
import fr.ght1pc9kc.baywatch.security.infra.adapters.SpringAuthenticationContext;
import fr.ght1pc9kc.baywatch.techwatch.api.model.WebFeed;
import fr.ght1pc9kc.baywatch.techwatch.infra.persistence.FeedRepository;
import fr.ght1pc9kc.baywatch.tests.samples.FeedSamples;
import fr.ght1pc9kc.baywatch.tests.samples.UserSamples;
import fr.ght1pc9kc.entity.api.Entity;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.nio.charset.StandardCharsets;
import java.util.Collection;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

class OpmlServiceImplTest {

    private OpmlService tested;

    private AuthenticationFacade authenticationFacadeMock;
    private FeedRepository feedRepositoryMock;

    @BeforeEach
    void setUp() {
        feedRepositoryMock = mock(FeedRepository.class);
        doReturn(Flux.just(FeedSamples.JEDI)).when(feedRepositoryMock).list(any(QueryContext.class));
        doAnswer(answer -> Flux.fromIterable(answer.getArgument(0))).when(feedRepositoryMock).persist(any());
        doAnswer(answer -> Flux.fromIterable(answer.getArgument(1))).when(feedRepositoryMock).persistUserRelation(anyString(), any());

        authenticationFacadeMock = spy(new SpringAuthenticationContext());
        tested = new OpmlServiceImpl(feedRepositoryMock, authenticationFacadeMock, OpmlWriter::new, OpmlReader::new);
    }

    @Test
    void should_export_opml() {
        doReturn(Mono.just(UserSamples.OBIWAN)).when(authenticationFacadeMock).getConnectedUser();

        StepVerifier.create(tested.opmlExport())
                .assertNext(actual -> Assertions.assertThat(actual).asString(StandardCharsets.UTF_8)
                        .contains("<ownerName>Obiwan Kenobi</ownerName>")
                        .contains("<title>Baywatch OPML export</title>")
                        .containsIgnoringNewLines("""
                                <outline text="Jedi Feed" type="rss" xmlUrl="https://www.jedi.com/" title="Jedi Feed" category=""/>
                                """))
                .verifyComplete();
    }

    @Test
    @SuppressWarnings("unchecked")
    void should_import_opml() {
        doReturn(Mono.just(UserSamples.OBIWAN)).when(authenticationFacadeMock).getConnectedUser();

        StepVerifier.create(tested.opmlImport(() -> OpmlServiceImplTest.class.getResourceAsStream("okenobi.xml")))
                .verifyComplete();

        ArgumentCaptor<Collection<Entity<WebFeed>>> captor = ArgumentCaptor.forClass(Collection.class);
        verify(feedRepositoryMock).persist(captor.capture());
        Assertions.assertThat(captor.getValue()).hasSize(30);
    }
}