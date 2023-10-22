package fr.ght1pc9kc.baywatch.scraper.domain;

import fr.ght1pc9kc.baywatch.notify.api.NotifyService;
import fr.ght1pc9kc.baywatch.scraper.api.ScrapEnrichmentService;
import fr.ght1pc9kc.baywatch.security.api.AuthenticationFacade;
import fr.ght1pc9kc.baywatch.techwatch.api.SystemMaintenanceService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import reactor.core.scheduler.Schedulers;

import java.util.List;

class ScrapEnrichmentServiceImplTest {

    private ScrapEnrichmentService tested;

    @BeforeEach
    void setUp() {
        AuthenticationFacade mockAuthentFacade = Mockito.mock(AuthenticationFacade.class);
        SystemMaintenanceService mockSystemMaintenance = Mockito.mock(SystemMaintenanceService.class);
        NotifyService mockNotifyService = Mockito.mock(NotifyService.class);
        tested = new ScrapEnrichmentServiceImpl(
                List.of(), List.of(), mockAuthentFacade, mockSystemMaintenance, mockNotifyService, Schedulers.immediate());
    }

    @Test
    void should_scrap_single_news() {

    }
}