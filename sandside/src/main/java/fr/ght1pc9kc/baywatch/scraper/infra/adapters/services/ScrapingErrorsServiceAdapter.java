package fr.ght1pc9kc.baywatch.scraper.infra.adapters.services;

import fr.ght1pc9kc.baywatch.scraper.api.ScrapingErrorsService;
import fr.ght1pc9kc.baywatch.scraper.domain.ScrapingErrorsServiceImpl;
import fr.ght1pc9kc.baywatch.scraper.domain.ports.ScrapingAuthentFacade;
import fr.ght1pc9kc.baywatch.scraper.domain.ports.ScrapingErrorPersistencePort;
import lombok.experimental.Delegate;
import org.springframework.stereotype.Service;

@Service
public class ScrapingErrorsServiceAdapter implements ScrapingErrorsService {
    @Delegate
    private final ScrapingErrorsService delegate;

    public ScrapingErrorsServiceAdapter(
            ScrapingErrorPersistencePort scrapingErrorPersistencePort, ScrapingAuthentFacade scrapingAuthentFacade) {
        this.delegate = new ScrapingErrorsServiceImpl(scrapingErrorPersistencePort, scrapingAuthentFacade);
    }
}
