package fr.ght1pc9kc.baywatch.techwatch.infra.adapters;

import fr.ght1pc9kc.baywatch.security.api.AuthenticationFacade;
import fr.ght1pc9kc.baywatch.techwatch.api.StatService;
import fr.ght1pc9kc.baywatch.techwatch.domain.StatServiceImpl;
import fr.ght1pc9kc.baywatch.techwatch.domain.ports.FeedPersistencePort;
import fr.ght1pc9kc.baywatch.techwatch.domain.ports.NewsPersistencePort;
import lombok.experimental.Delegate;
import org.springframework.stereotype.Service;

@Service
public class StatisticsServiceAdapter implements StatService {
    @Delegate
    private final StatService delegate;

    public StatisticsServiceAdapter(FeedPersistencePort feedRepository, NewsPersistencePort newsRepository, AuthenticationFacade authFacade) {
        this.delegate = new StatServiceImpl(feedRepository, newsRepository, authFacade);
    }
}
