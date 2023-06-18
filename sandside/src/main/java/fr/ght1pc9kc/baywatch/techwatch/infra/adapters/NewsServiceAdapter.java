package fr.ght1pc9kc.baywatch.techwatch.infra.adapters;

import fr.ght1pc9kc.baywatch.security.api.AuthenticationFacade;
import fr.ght1pc9kc.baywatch.techwatch.api.NewsService;
import fr.ght1pc9kc.baywatch.techwatch.domain.NewsServiceImpl;
import fr.ght1pc9kc.baywatch.techwatch.domain.ports.FeedPersistencePort;
import fr.ght1pc9kc.baywatch.techwatch.domain.ports.NewsPersistencePort;
import fr.ght1pc9kc.baywatch.techwatch.domain.ports.StatePersistencePort;
import fr.ght1pc9kc.baywatch.techwatch.domain.ports.TeamServicePort;
import fr.ght1pc9kc.juery.api.filter.CriteriaVisitor;
import lombok.experimental.Delegate;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class NewsServiceAdapter implements NewsService {
    @Delegate
    private final NewsService delegate;

    public NewsServiceAdapter(CriteriaVisitor<List<String>> propertiesExtractor, NewsPersistencePort newsRepository,
                              FeedPersistencePort feedRepository, StatePersistencePort stateRepository, AuthenticationFacade authFacade,
                              TeamServicePort teamServicePort) {
        this.delegate = new NewsServiceImpl(propertiesExtractor, newsRepository, feedRepository, stateRepository, authFacade, teamServicePort);
    }
}
