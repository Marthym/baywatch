package fr.ght1pc9kc.baywatch.scraper.infra.adapters;

import fr.ght1pc9kc.baywatch.scraper.api.NewsEnrichmentService;
import fr.ght1pc9kc.baywatch.scraper.api.NewsFilter;
import fr.ght1pc9kc.baywatch.scraper.domain.NewsEnrichmentServiceImpl;
import fr.ght1pc9kc.baywatch.security.api.AuthenticationFacade;
import fr.ght1pc9kc.baywatch.techwatch.api.SystemMaintenanceService;
import lombok.experimental.Delegate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class NewsEnrichmentAdapter implements NewsEnrichmentService {

    @Delegate
    NewsEnrichmentService delegate;

    @Autowired
    public NewsEnrichmentAdapter(List<NewsFilter> filters, AuthenticationFacade facade, SystemMaintenanceService maintenanceService) {
        this.delegate = new NewsEnrichmentServiceImpl(filters, facade, maintenanceService);
    }
}
