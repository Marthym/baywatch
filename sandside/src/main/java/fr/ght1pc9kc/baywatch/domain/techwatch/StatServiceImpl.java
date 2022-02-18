package fr.ght1pc9kc.baywatch.domain.techwatch;

import fr.ght1pc9kc.baywatch.api.techwatch.StatService;
import fr.ght1pc9kc.baywatch.api.common.model.EntitiesProperties;
import fr.ght1pc9kc.baywatch.domain.security.exceptions.UnauthenticatedUser;
import fr.ght1pc9kc.baywatch.domain.security.ports.AuthenticationFacade;
import fr.ght1pc9kc.baywatch.domain.techwatch.model.QueryContext;
import fr.ght1pc9kc.baywatch.domain.techwatch.ports.FeedPersistencePort;
import fr.ght1pc9kc.baywatch.domain.techwatch.ports.NewsPersistencePort;
import fr.ght1pc9kc.juery.api.Criteria;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class StatServiceImpl implements StatService {
    private final FeedPersistencePort feedRepository;
    private final NewsPersistencePort newsRepository;
    private final AuthenticationFacade authFacade;

    @Override
    public Mono<Integer> getNewsCount() {
        QueryContext qCtx = QueryContext.empty();
        return authFacade.getConnectedUser()
                .switchIfEmpty(Mono.error(new UnauthenticatedUser("Authentication not found !")))
                .map(user -> qCtx.withUserId(user.id))
                .onErrorResume(UnauthenticatedUser.class, e -> Mono.just(qCtx))
                .flatMap(newsRepository::count);
    }

    @Override
    public Mono<Integer> getFeedsCount() {
        QueryContext qCtx = QueryContext.empty();
        return authFacade.getConnectedUser()
                .switchIfEmpty(Mono.error(new UnauthenticatedUser("Authentication not found !")))
                .map(user -> qCtx.withUserId(user.id))
                .onErrorResume(UnauthenticatedUser.class, e -> Mono.just(qCtx))
                .flatMap(feedRepository::count);
    }

    @Override
    public Mono<Integer> getUnreadCount() {
        QueryContext qCtx = QueryContext.all(Criteria.property(EntitiesProperties.READ).eq(false));
        return authFacade.getConnectedUser()
                .switchIfEmpty(Mono.error(new UnauthenticatedUser("Authentication not found !")))
                .map(user -> qCtx.withUserId(user.id))
                .flatMap(newsRepository::count)
                .onErrorResume(UnauthenticatedUser.class, e -> Mono.just(0));
    }
}
