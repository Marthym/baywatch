package fr.ght1pc9kc.baywatch.scraper.infra.adapters;

import fr.ght1pc9kc.baywatch.scraper.api.ScrapingHandler;
import fr.ght1pc9kc.baywatch.scraper.domain.actions.DeleteOrphanFeedHandler;
import fr.ght1pc9kc.baywatch.techwatch.api.SystemMaintenanceService;
import lombok.experimental.Delegate;
import org.springframework.stereotype.Component;

@Component
public class DeleteOrphanFeedHandlerAdapter implements ScrapingHandler {
    @Delegate
    private final ScrapingHandler delegate;

    public DeleteOrphanFeedHandlerAdapter(SystemMaintenanceService maintenanceService) {
        this.delegate = new DeleteOrphanFeedHandler(maintenanceService);
    }
}
