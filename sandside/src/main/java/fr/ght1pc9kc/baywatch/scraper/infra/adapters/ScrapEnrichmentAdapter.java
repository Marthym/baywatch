package fr.ght1pc9kc.baywatch.scraper.infra.adapters;

import fr.ght1pc9kc.baywatch.scraper.api.NewsFilter;
import fr.ght1pc9kc.baywatch.scraper.api.ScrapEnrichmentService;
import fr.ght1pc9kc.baywatch.scraper.domain.ScrapEnrichmentServiceImpl;
import fr.ght1pc9kc.baywatch.scraper.domain.model.FeedsFilter;
import fr.ght1pc9kc.baywatch.security.api.AuthenticationFacade;
import fr.ght1pc9kc.baywatch.techwatch.api.SystemMaintenanceService;
import lombok.experimental.Delegate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ScrapEnrichmentAdapter implements ScrapEnrichmentService {

    @Delegate
    ScrapEnrichmentService delegate;

    @Autowired
    public ScrapEnrichmentAdapter(
            List<NewsFilter> newsFilters, List<FeedsFilter> feedsFilters, AuthenticationFacade facade, SystemMaintenanceService maintenanceService) {
        this.delegate = new ScrapEnrichmentServiceImpl(newsFilters, feedsFilters, facade, maintenanceService);
    }
}