package fr.ght1pc9kc.baywatch.techwatch.domain;

import fr.ght1pc9kc.baywatch.common.api.exceptions.UnauthorizedException;
import fr.ght1pc9kc.baywatch.common.api.model.FeedMeta;
import fr.ght1pc9kc.baywatch.security.api.AuthenticationFacade;
import fr.ght1pc9kc.baywatch.security.api.model.Role;
import fr.ght1pc9kc.baywatch.security.api.model.RoleUtils;
import fr.ght1pc9kc.baywatch.techwatch.api.SystemMaintenanceService;
import fr.ght1pc9kc.baywatch.techwatch.api.model.News;
import fr.ght1pc9kc.baywatch.techwatch.api.model.WebFeed;
import fr.ght1pc9kc.baywatch.techwatch.domain.model.QueryContext;
import fr.ght1pc9kc.baywatch.techwatch.domain.ports.FeedPersistencePort;
import fr.ght1pc9kc.baywatch.techwatch.domain.ports.NewsPersistencePort;
import fr.ght1pc9kc.baywatch.techwatch.infra.model.FeedDeletedResult;
import fr.ght1pc9kc.entity.api.Entity;
import fr.ght1pc9kc.juery.api.Criteria;
import fr.ght1pc9kc.juery.api.PageRequest;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Collection;
import java.util.Map;

import static fr.ght1pc9kc.baywatch.common.api.model.EntitiesProperties.FEED_ID;

@RequiredArgsConstructor
public class SystemMaintenanceServiceImpl implements SystemMaintenanceService {
    private static final String EXCEPTION_MESSAGE = "Operation not permitted for user !";

    private final FeedPersistencePort feedRepository;
    private final NewsPersistencePort newsRepository;
    private final AuthenticationFacade authFacade;

    @Override
    public Flux<Entity<WebFeed>> feedList() {
        return feedList(PageRequest.all());
    }

    @Override
    public Flux<Entity<WebFeed>> feedList(PageRequest pageRequest) {
        return authFacade.getConnectedUser()
                .filter(user -> RoleUtils.hasRole(user.self(), Role.SYSTEM))
                .switchIfEmpty(Mono.error(() -> new UnauthorizedException(EXCEPTION_MESSAGE)))
                .flatMapMany(u -> feedRepository.list(QueryContext.from(pageRequest)));
    }

    @Override
    public Mono<Integer> feedDelete(Collection<String> toDelete) {
        return authFacade.getConnectedUser()
                .filter(user -> RoleUtils.hasRole(user.self(), Role.SYSTEM))
                .switchIfEmpty(Mono.error(() -> new UnauthorizedException(EXCEPTION_MESSAGE)))
                .map(u -> QueryContext.all(Criteria.property(FEED_ID).in(toDelete)))
                .flatMap(feedRepository::delete)
                .map(FeedDeletedResult::purged);
    }

    @Override
    public Mono<Entity<WebFeed>> feedUpdate(String id, WebFeed toPersist) {
        return authFacade.getConnectedUser()
                .filter(user -> RoleUtils.hasRole(user.self(), Role.SYSTEM))
                .switchIfEmpty(Mono.error(() -> new UnauthorizedException(EXCEPTION_MESSAGE)))
                .flatMap(q -> feedRepository.update(id, toPersist));
    }

    @Override
    public Mono<Entity<WebFeed>> feedUpdateMetas(String id, Map<FeedMeta, Object> metas) {
        return authFacade.getConnectedUser()
                .filter(user -> RoleUtils.hasRole(user.self(), Role.SYSTEM))
                .switchIfEmpty(Mono.error(() -> new UnauthorizedException(EXCEPTION_MESSAGE)))
                .flatMap(q -> feedRepository.updateMetas(id, metas));
    }

    @Override
    public Flux<News> newsList(PageRequest pageRequest) {
        return authFacade.getConnectedUser()
                .filter(user -> RoleUtils.hasRole(user.self(), Role.SYSTEM))
                .switchIfEmpty(Mono.error(() -> new UnauthorizedException(EXCEPTION_MESSAGE)))
                .flatMapMany(u -> newsRepository.list(QueryContext.from(pageRequest)));
    }

    @Override
    public Flux<String> newsIdList(PageRequest pageRequest) {
        return authFacade.getConnectedUser()
                .filter(user -> RoleUtils.hasRole(user.self(), Role.SYSTEM))
                .switchIfEmpty(Mono.error(() -> new UnauthorizedException(EXCEPTION_MESSAGE)))
                .flatMapMany(u -> newsRepository.listId(QueryContext.from(pageRequest)));
    }

    @Override
    public Mono<Integer> newsLoad(Collection<News> toLoad) {
        return authFacade.getConnectedUser()
                .filter(user -> RoleUtils.hasRole(user.self(), Role.SYSTEM))
                .switchIfEmpty(Mono.error(() -> new UnauthorizedException(EXCEPTION_MESSAGE)))
                .flatMap(user -> newsRepository.persist(toLoad));
    }

    @Override
    public Mono<Integer> newsDelete(Collection<String> toDelete) {
        return authFacade.getConnectedUser()
                .filter(user -> RoleUtils.hasRole(user.self(), Role.SYSTEM))
                .switchIfEmpty(Mono.error(() -> new UnauthorizedException(EXCEPTION_MESSAGE)))
                .flatMap(user -> newsRepository.delete(toDelete));
    }
}
