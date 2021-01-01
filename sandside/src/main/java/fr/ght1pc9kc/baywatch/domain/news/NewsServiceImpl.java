package fr.ght1pc9kc.baywatch.domain.news;

import fr.ght1pc9kc.baywatch.api.NewsService;
import fr.ght1pc9kc.baywatch.api.model.News;
import fr.ght1pc9kc.baywatch.api.model.State;
import fr.ght1pc9kc.baywatch.api.model.request.PageRequest;
import fr.ght1pc9kc.baywatch.api.model.request.filter.Criteria;
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
                .flatMapMany(u -> newsRepository.userList(pageRequest))
                .switchIfEmpty(Flux.defer(() ->
                        newsRepository.list(pageRequest).map(rn -> News.builder()
                                .raw(rn)
                                .state(State.NONE)
                                .build())
                ));
    }

    @Override
    public Mono<News> get(String id) {
        return list(PageRequest.one(Criteria.property(ID).eq(id))).next();
    }
}
