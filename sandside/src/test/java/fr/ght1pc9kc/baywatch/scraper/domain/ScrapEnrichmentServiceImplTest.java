package fr.ght1pc9kc.baywatch.scraper.domain;

import fr.ght1pc9kc.baywatch.notify.api.NotifyService;
import fr.ght1pc9kc.baywatch.scraper.api.ScrapEnrichmentService;
import fr.ght1pc9kc.baywatch.security.api.AuthenticationFacade;
import fr.ght1pc9kc.baywatch.techwatch.api.SystemMaintenanceService;
import fr.ght1pc9kc.baywatch.tests.samples.NewsSamples;
import fr.ght1pc9kc.baywatch.tests.samples.UserSamples;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import reactor.test.StepVerifier;

import java.util.List;

import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ScrapEnrichmentServiceImplTest {

    private ScrapEnrichmentService tested;
    private SystemMaintenanceService mockSystemMaintenance;

    @BeforeEach
    void setUp() {
        AuthenticationFacade mockAuthentFacade = Mockito.mock(AuthenticationFacade.class);
        when(mockAuthentFacade.getConnectedUser()).thenReturn(Mono.just(UserSamples.YODA));
        mockSystemMaintenance = Mockito.mock(SystemMaintenanceService.class);
        NotifyService mockNotifyService = Mockito.mock(NotifyService.class);
        tested = new ScrapEnrichmentServiceImpl(
                List.of(), List.of(), mockAuthentFacade, mockSystemMaintenance, mockNotifyService, Schedulers.immediate());
    }

    @Test
    void should_scrap_single_news() {
        StepVerifier.create(tested.scrapSingleNews(NewsSamples.MAY_THE_FORCE.getRaw().link()))
                .verifyComplete();

        verify(mockSystemMaintenance).newsLoad(anyCollection());
    }
}