package fr.ght1pc9kc.baywatch.infra.adapters;

import fr.ght1pc9kc.baywatch.api.FeedService;
import fr.ght1pc9kc.baywatch.domain.FeedServiceImpl;
import fr.ght1pc9kc.baywatch.domain.ports.AuthenticationFacade;
import fr.ght1pc9kc.baywatch.domain.ports.FeedPersistencePort;
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
