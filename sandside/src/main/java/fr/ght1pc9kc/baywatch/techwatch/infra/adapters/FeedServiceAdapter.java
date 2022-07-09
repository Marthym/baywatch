package fr.ght1pc9kc.baywatch.techwatch.infra.adapters;

import fr.ght1pc9kc.baywatch.techwatch.api.FeedService;
import fr.ght1pc9kc.baywatch.techwatch.domain.FeedServiceImpl;
import fr.ght1pc9kc.baywatch.security.api.AuthenticationFacade;
import fr.ght1pc9kc.baywatch.techwatch.domain.ports.FeedPersistencePort;
import fr.ght1pc9kc.baywatch.techwatch.domain.ports.ScraperServicePort;
import lombok.experimental.Delegate;
import org.springframework.stereotype.Service;

@Service
public class FeedServiceAdapter implements FeedService {

    @Delegate
    private final FeedService delegate;

    public FeedServiceAdapter(FeedPersistencePort feedRepository, ScraperServicePort scraperServicePort, AuthenticationFacade authFacade) {
        this.delegate = new FeedServiceImpl(feedRepository, scraperServicePort, authFacade);
    }
}
