package fr.ght1pc9kc.baywatch.techwatch.domain;

import fr.ght1pc9kc.baywatch.common.api.model.EntitiesProperties;
import fr.ght1pc9kc.baywatch.security.api.AuthenticationFacade;
import fr.ght1pc9kc.baywatch.security.domain.exceptions.UnauthenticatedUser;
import fr.ght1pc9kc.baywatch.techwatch.api.StatService;
import fr.ght1pc9kc.baywatch.techwatch.domain.model.QueryContext;
import fr.ght1pc9kc.baywatch.techwatch.domain.ports.FeedPersistencePort;
import fr.ght1pc9kc.baywatch.techwatch.domain.ports.NewsPersistencePort;
import fr.ght1pc9kc.juery.api.Criteria;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
public class StatServiceImpl implements StatService {
    public static final String AUTHENTICATION_NOT_FOUND = "Authentication not found !";
    private final FeedPersistencePort feedRepository;
    private final NewsPersistencePort newsRepository;
    private final AuthenticationFacade authFacade;

    @Override
    public Mono<Integer> getNewsCount() {
        QueryContext qCtx = QueryContext.empty();
        return authFacade.getConnectedUser()
                .switchIfEmpty(Mono.error(new UnauthenticatedUser(AUTHENTICATION_NOT_FOUND)))
                .map(user -> qCtx.withUserId(user.id))
                .onErrorResume(UnauthenticatedUser.class, e -> Mono.just(qCtx))
                .flatMap(newsRepository::count);
    }

    @Override
    public Mono<Integer> getFeedsCount() {
        QueryContext qCtx = QueryContext.empty();
        return authFacade.getConnectedUser()
                .switchIfEmpty(Mono.error(new UnauthenticatedUser(AUTHENTICATION_NOT_FOUND)))
                .map(user -> qCtx.withUserId(user.id))
                .onErrorResume(UnauthenticatedUser.class, e -> Mono.just(qCtx))
                .flatMap(feedRepository::count);
    }

    @Override
    public Mono<Integer> getUnreadCount() {
        QueryContext qCtx = QueryContext.all(Criteria.property(EntitiesProperties.READ).eq(false));
        return authFacade.getConnectedUser()
                .switchIfEmpty(Mono.error(new UnauthenticatedUser(AUTHENTICATION_NOT_FOUND)))
                .map(user -> qCtx.withUserId(user.id))
                .flatMap(newsRepository::count)
                .onErrorResume(UnauthenticatedUser.class, e -> Mono.just(0));
    }
}
