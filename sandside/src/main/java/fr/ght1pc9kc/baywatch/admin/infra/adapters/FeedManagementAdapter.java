package fr.ght1pc9kc.baywatch.admin.infra.adapters;

import fr.ght1pc9kc.baywatch.admin.api.FeedManagementService;
import fr.ght1pc9kc.baywatch.admin.domain.ports.RawFeedPersistencePort;
import fr.ght1pc9kc.baywatch.admin.domain.services.FeedManagementServiceImpl;
import lombok.experimental.Delegate;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

@Service
@PreAuthorize("hasRole('ADMIN')")
public class FeedManagementAdapter implements FeedManagementService {
    @Delegate
    private final FeedManagementService delegate;

    public FeedManagementAdapter(RawFeedPersistencePort feedRepository, RawNewsPersistenceAdapter newsRepository) {
        this.delegate = new FeedManagementServiceImpl(feedRepository, newsRepository);
    }
}
