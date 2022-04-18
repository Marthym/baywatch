package fr.ght1pc9kc.baywatch.admin.domain;

import fr.ght1pc9kc.baywatch.admin.api.StatisticsService;
import fr.ght1pc9kc.baywatch.common.api.exceptions.UnauthorizedException;
import fr.ght1pc9kc.baywatch.security.api.AuthenticationFacade;
import fr.ght1pc9kc.baywatch.security.api.UserService;
import fr.ght1pc9kc.baywatch.techwatch.api.FeedService;
import fr.ght1pc9kc.baywatch.techwatch.api.NewsService;
import fr.ght1pc9kc.juery.api.PageRequest;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
public class StatisticsServiceImpl implements StatisticsService {

    private final FeedService feedService;
    private final NewsService newsService;
    private final UserService userService;
    private final AuthenticationFacade authFacade;

    @Override
    public Mono<Integer> getNewsCount() {
        return authFacade.getConnectedUser()
                .switchIfEmpty(Mono.error(new UnauthorizedException()))
                .map(user -> PageRequest.all())
                .flatMap(newsService::count);
    }

    @Override
    public Mono<Integer> getFeedsCount() {
        return authFacade.getConnectedUser()
                .switchIfEmpty(Mono.error(new UnauthorizedException()))
                .map(user -> PageRequest.all())
                .flatMap(feedService::count);
    }

    @Override
    public Mono<Integer> getUsersCount() {
        return authFacade.getConnectedUser()
                .switchIfEmpty(Mono.error(new UnauthorizedException()))
                .map(user -> PageRequest.all())
                .flatMap(userService::count);
    }
}
