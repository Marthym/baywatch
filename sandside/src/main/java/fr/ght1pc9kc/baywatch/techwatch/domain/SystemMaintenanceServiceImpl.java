package fr.ght1pc9kc.baywatch.techwatch.domain;

import fr.ght1pc9kc.baywatch.common.api.exceptions.UnauthorizedException;
import fr.ght1pc9kc.baywatch.security.api.AuthenticationFacade;
import fr.ght1pc9kc.baywatch.security.api.model.Role;
import fr.ght1pc9kc.baywatch.techwatch.api.SystemMaintenanceService;
import fr.ght1pc9kc.baywatch.techwatch.api.model.Feed;
import fr.ght1pc9kc.baywatch.techwatch.api.model.News;
import fr.ght1pc9kc.baywatch.techwatch.api.model.RawFeed;
import fr.ght1pc9kc.baywatch.techwatch.api.model.RawNews;
import fr.ght1pc9kc.baywatch.techwatch.domain.model.QueryContext;
import fr.ght1pc9kc.baywatch.techwatch.domain.ports.FeedPersistencePort;
import fr.ght1pc9kc.baywatch.techwatch.domain.ports.NewsPersistencePort;
import fr.ght1pc9kc.juery.api.Criteria;
import fr.ght1pc9kc.juery.api.PageRequest;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Collection;

import static fr.ght1pc9kc.baywatch.common.api.model.EntitiesProperties.FEED_ID;

@RequiredArgsConstructor
public class SystemMaintenanceServiceImpl implements SystemMaintenanceService {
    private static final String EXCEPTION_MESSAGE = "Operation not permitted for user !";

    private final FeedPersistencePort feedRepository;
    private final NewsPersistencePort newsRepository;
    private final AuthenticationFacade authFacade;

    @Override
    public Flux<RawFeed> feedList() {
        return feedList(PageRequest.all());
    }

    @Override
    public Flux<RawFeed> feedList(PageRequest pageRequest) {
        return authFacade.getConnectedUser()
                .filter(user -> Role.SYSTEM == user.self.role)
                .switchIfEmpty(Mono.error(() -> new UnauthorizedException(EXCEPTION_MESSAGE)))
                .flatMapMany(u -> feedRepository.list(QueryContext.from(pageRequest)))
                .map(Feed::getRaw);
    }

    @Override
    public Mono<Integer> feedDelete(Collection<String> toDelete) {
        return authFacade.getConnectedUser()
                .filter(user -> Role.SYSTEM == user.self.role)
                .switchIfEmpty(Mono.error(() -> new UnauthorizedException(EXCEPTION_MESSAGE)))
                .map(u -> QueryContext.all(Criteria.property(FEED_ID).in(toDelete)))
                .flatMap(feedRepository::delete)
                .map(r -> r.purged);
    }

    @Override
    public Flux<RawNews> newsList(PageRequest pageRequest) {
        return authFacade.getConnectedUser()
                .filter(user -> Role.SYSTEM == user.self.role)
                .switchIfEmpty(Mono.error(() -> new UnauthorizedException(EXCEPTION_MESSAGE)))
                .flatMapMany(u -> newsRepository.list(QueryContext.from(pageRequest)))
                .map(News::getRaw);
    }

    @Override
    public Flux<String> newsIdList(PageRequest pageRequest) {
        return authFacade.getConnectedUser()
                .filter(user -> Role.SYSTEM == user.self.role)
                .switchIfEmpty(Mono.error(() -> new UnauthorizedException(EXCEPTION_MESSAGE)))
                .flatMapMany(u -> newsRepository.listId(QueryContext.from(pageRequest)));
    }

    @Override
    public Mono<Integer> newsLoad(Collection<News> toLoad) {
        return authFacade.getConnectedUser()
                .filter(user -> Role.SYSTEM == user.self.role)
                .switchIfEmpty(Mono.error(() -> new UnauthorizedException(EXCEPTION_MESSAGE)))
                .flatMap(user -> newsRepository.persist(toLoad));
    }

    @Override
    public Mono<Integer> newsDelete(Collection<String> toDelete) {
        return authFacade.getConnectedUser()
                .filter(user -> Role.SYSTEM == user.self.role)
                .switchIfEmpty(Mono.error(() -> new UnauthorizedException(EXCEPTION_MESSAGE)))
                .flatMap(user -> newsRepository.delete(toDelete));
    }
}
