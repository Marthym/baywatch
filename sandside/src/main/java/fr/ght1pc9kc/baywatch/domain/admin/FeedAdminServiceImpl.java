package fr.ght1pc9kc.baywatch.domain.admin;

import fr.ght1pc9kc.baywatch.api.admin.FeedAdminService;
import fr.ght1pc9kc.baywatch.api.model.RawFeed;
import fr.ght1pc9kc.baywatch.api.security.model.Role;
import fr.ght1pc9kc.baywatch.api.security.model.RoleUtils;
import fr.ght1pc9kc.baywatch.domain.admin.ports.FeedAdministrationPort;
import fr.ght1pc9kc.baywatch.domain.exceptions.UnauthenticatedUser;
import fr.ght1pc9kc.baywatch.domain.ports.AuthenticationFacade;
import fr.ght1pc9kc.juery.api.PageRequest;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Collection;

@RequiredArgsConstructor
public class FeedAdminServiceImpl implements FeedAdminService {

    private final FeedAdministrationPort feedRepository;
    private final AuthenticationFacade authFacade;

    @Override
    public Mono<RawFeed> get(String id) {
        return authFacade.getConnectedUser()
                .switchIfEmpty(Mono.error(new UnauthenticatedUser("Authentication not found !")))
                .map(u -> RoleUtils.hasRoleOrThrow(u, Role.ADMIN))
                .flatMap(u -> feedRepository.get(id));
    }

    @Override
    public Flux<RawFeed> list() {
        return list(PageRequest.all());
    }

    @Override
    public Flux<RawFeed> list(PageRequest pageRequest) {
        return authFacade.getConnectedUser()
                .switchIfEmpty(Mono.error(new UnauthenticatedUser("Authentication not found !")))
                .map(u -> RoleUtils.hasRoleOrThrow(u, Role.ADMIN))
                .flatMapMany(u -> feedRepository.list(pageRequest));
    }

    @Override
    public Mono<Integer> delete(Collection<String> toDelete) {
        return authFacade.getConnectedUser()
                .switchIfEmpty(Mono.error(new UnauthenticatedUser("Authentication not found !")))
                .map(u -> RoleUtils.hasRoleOrThrow(u, Role.ADMIN))
                .flatMap(u -> feedRepository.delete(toDelete));
    }
}
