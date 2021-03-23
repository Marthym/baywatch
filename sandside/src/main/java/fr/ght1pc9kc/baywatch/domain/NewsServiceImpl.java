package fr.ght1pc9kc.baywatch.domain;

import fr.ght1pc9kc.baywatch.api.NewsService;
import fr.ght1pc9kc.baywatch.api.model.News;
import fr.ght1pc9kc.baywatch.api.model.State;
import fr.ght1pc9kc.baywatch.api.model.request.PageRequest;
import fr.ght1pc9kc.baywatch.api.model.request.filter.Criteria;
import fr.ght1pc9kc.baywatch.domain.exceptions.UnauthenticatedUser;
import fr.ght1pc9kc.baywatch.domain.ports.AuthenticationFacade;
import fr.ght1pc9kc.baywatch.domain.ports.NewsPersistencePort;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import static fr.ght1pc9kc.baywatch.infra.mappers.PropertiesMappers.ID;

@Service
@AllArgsConstructor
public class NewsServiceImpl implements NewsService {
    private final NewsPersistencePort newsRepository;
    private final AuthenticationFacade authFacade;

    @Override
    public Flux<News> list(PageRequest pageRequest) {
        return authFacade.getConnectedUser()
                .switchIfEmpty(Mono.error(new UnauthenticatedUser("Authentication not found !")))
                .flatMapMany(u -> newsRepository.userList(pageRequest))
                .onErrorResume(UnauthenticatedUser.class, (e) ->
                        newsRepository.list(pageRequest).map(rn -> News.builder()
                                .raw(rn)
                                .state(State.NONE)
                                .build())
                );
    }

    @Override
    public Mono<News> get(String id) {
        return list(PageRequest.one(Criteria.property(ID).eq(id))).next();
    }

    @Override
    public Mono<News> mark(String id, int flag) {
        return authFacade.getConnectedUser()
                .switchIfEmpty(Mono.error(new UnauthenticatedUser("Authentication not found !")))
                .flatMap(user -> newsRepository.addStateFlag(id, user.id, flag))
                .flatMap(state -> get(id));
    }

    @Override
    public Mono<News> unmark(String id, int flag) {
        return authFacade.getConnectedUser()
                .switchIfEmpty(Mono.error(new UnauthenticatedUser("Authentication not found !")))
                .flatMap(user -> newsRepository.removeStateFlag(id, user.id, flag))
                .flatMap(state -> get(id));
    }
}
