package fr.ght1pc9kc.baywatch.techwatch.infra.adapters;

import fr.ght1pc9kc.baywatch.admin.api.StatisticsService;
import fr.ght1pc9kc.baywatch.admin.domain.StatisticsServiceImpl;
import fr.ght1pc9kc.baywatch.security.api.AuthenticationFacade;
import fr.ght1pc9kc.baywatch.security.api.UserService;
import fr.ght1pc9kc.baywatch.techwatch.api.FeedService;
import fr.ght1pc9kc.baywatch.techwatch.api.NewsService;
import lombok.experimental.Delegate;
import org.springframework.stereotype.Service;

@Service
public class StatisticsServiceAdapter implements StatisticsService {
    @Delegate
    private final StatisticsService delegate;

    public StatisticsServiceAdapter(FeedService feedRepository, NewsService newsRepository,
                                    UserService userService, AuthenticationFacade authFacade) {
        this.delegate = new StatisticsServiceImpl(feedRepository, newsRepository, userService, authFacade);
    }
}
