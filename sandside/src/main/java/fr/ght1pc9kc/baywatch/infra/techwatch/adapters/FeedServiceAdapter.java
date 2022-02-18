package fr.ght1pc9kc.baywatch.infra.techwatch.adapters;

import fr.ght1pc9kc.baywatch.api.techwatch.FeedService;
import fr.ght1pc9kc.baywatch.domain.techwatch.FeedServiceImpl;
import fr.ght1pc9kc.baywatch.domain.security.ports.AuthenticationFacade;
import fr.ght1pc9kc.baywatch.domain.techwatch.ports.FeedPersistencePort;
import lombok.experimental.Delegate;
import org.springframework.stereotype.Service;

@Service
public class FeedServiceAdapter implements FeedService {

    @Delegate
    private final FeedService delegate;

    public FeedServiceAdapter(FeedPersistencePort feedRepository, AuthenticationFacade authFacade) {
        this.delegate = new FeedServiceImpl(feedRepository, authFacade);
    }
}
