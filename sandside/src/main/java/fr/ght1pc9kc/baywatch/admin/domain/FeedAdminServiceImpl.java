package fr.ght1pc9kc.baywatch.admin.domain;

import fr.ght1pc9kc.baywatch.admin.api.FeedAdminService;
import fr.ght1pc9kc.baywatch.techwatch.api.model.Feed;
import fr.ght1pc9kc.baywatch.techwatch.api.model.RawFeed;
import fr.ght1pc9kc.baywatch.security.api.model.Role;
import fr.ght1pc9kc.baywatch.security.api.model.RoleUtils;
import fr.ght1pc9kc.baywatch.security.domain.exceptions.UnauthenticatedUser;
import fr.ght1pc9kc.baywatch.security.api.AuthenticationFacade;
import fr.ght1pc9kc.baywatch.techwatch.domain.model.QueryContext;
import fr.ght1pc9kc.baywatch.techwatch.domain.ports.FeedPersistencePort;
import fr.ght1pc9kc.juery.api.Criteria;
import fr.ght1pc9kc.juery.api.PageRequest;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Collection;

import static fr.ght1pc9kc.baywatch.common.api.model.EntitiesProperties.FEED_ID;

@RequiredArgsConstructor
public class FeedAdminServiceImpl implements FeedAdminService {

    private final FeedPersistencePort feedRepository;
    private final AuthenticationFacade authFacade;

    @Override
    public Mono<RawFeed> get(String id) {
        return authFacade.getConnectedUser()
                .switchIfEmpty(Mono.error(new UnauthenticatedUser("Authentication not found !")))
                .map(u -> RoleUtils.hasRoleOrThrow(u.self, Role.ADMIN))
                .flatMap(u -> feedRepository.get(QueryContext.id(id)))
                .map(Feed::getRaw);
    }

    @Override
    public Flux<RawFeed> list() {
        return list(PageRequest.all());
    }

    @Override
    public Flux<RawFeed> list(PageRequest pageRequest) {
        return authFacade.getConnectedUser()
                .switchIfEmpty(Mono.error(new UnauthenticatedUser("Authentication not found !")))
                .map(u -> RoleUtils.hasRoleOrThrow(u.self, Role.ADMIN))
                .flatMapMany(u -> feedRepository.list(QueryContext.from(pageRequest)))
                .map(Feed::getRaw);
    }

    @Override
    public Mono<Integer> delete(Collection<String> toDelete) {
        return authFacade.getConnectedUser()
                .switchIfEmpty(Mono.error(new UnauthenticatedUser("Authentication not found !")))
                .map(u -> RoleUtils.hasRoleOrThrow(u.self, Role.ADMIN))
                .map(u -> QueryContext.all(Criteria.property(FEED_ID).in(toDelete)))
                .flatMap(feedRepository::delete)
                .map(r -> r.purged);
    }
}
