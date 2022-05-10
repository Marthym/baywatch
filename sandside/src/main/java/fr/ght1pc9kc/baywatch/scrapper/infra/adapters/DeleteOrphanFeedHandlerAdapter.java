package fr.ght1pc9kc.baywatch.scrapper.infra.adapters;

import fr.ght1pc9kc.baywatch.scrapper.api.ScrappingHandler;
import fr.ght1pc9kc.baywatch.scrapper.domain.actions.DeleteOrphanFeedHandler;
import fr.ght1pc9kc.baywatch.techwatch.api.SystemMaintenanceService;
import lombok.experimental.Delegate;
import org.springframework.stereotype.Component;

@Component
public class DeleteOrphanFeedHandlerAdapter implements ScrappingHandler {
    @Delegate
    private final ScrappingHandler delegate;

    public DeleteOrphanFeedHandlerAdapter(SystemMaintenanceService maintenanceService) {
        this.delegate = new DeleteOrphanFeedHandler(maintenanceService);
    }
}
