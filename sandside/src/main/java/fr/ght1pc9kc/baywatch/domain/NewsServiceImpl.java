package fr.ght1pc9kc.baywatch.domain;

import fr.ght1pc9kc.baywatch.api.NewsService;
import fr.ght1pc9kc.baywatch.api.model.News;
import fr.ght1pc9kc.baywatch.api.model.State;
import fr.ght1pc9kc.baywatch.api.model.request.PageRequest;
import fr.ght1pc9kc.baywatch.api.model.request.filter.Criteria;
import fr.ght1pc9kc.baywatch.domain.exceptions.BadCriteriaFilter;
import fr.ght1pc9kc.baywatch.domain.exceptions.UnauthenticatedUser;
import fr.ght1pc9kc.baywatch.domain.ports.AuthenticationFacade;
import fr.ght1pc9kc.baywatch.domain.ports.NewsPersistencePort;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static java.util.function.Predicate.not;

@Service
@AllArgsConstructor
public class NewsServiceImpl implements NewsService {
    private static final Set<String> ALLOWED_CRITERIA = Set.of("id", "publication", "shared", "state", "title");
    private static final Set<String> ALLOWED_AUTHENTICATED_CRITERIA = Set.of("read");

    private final Criteria.Visitor<List<String>> propertiesExtractor;
    private final NewsPersistencePort newsRepository;
    private final AuthenticationFacade authFacade;

    @Override
    public Flux<News> list(PageRequest pageRequest) {
        return authFacade.getConnectedUser()
                .switchIfEmpty(Mono.error(new UnauthenticatedUser("Authentication not found !")))
                .map(x -> {
                    String bads = pageRequest.filter.visit(propertiesExtractor).stream()
                            .filter(not(ALLOWED_CRITERIA::contains))
                            .filter(not(ALLOWED_AUTHENTICATED_CRITERIA::contains))
                            .collect(Collectors.joining(", "));
                    if (!bads.isBlank()) {
                        throw new BadCriteriaFilter(String.format("Filters not allowed [ %s ]", bads));
                    } else {
                        return x;
                    }
                })
                .flatMapMany(u -> newsRepository.userList(pageRequest))
                .onErrorResume(UnauthenticatedUser.class, (e) -> {
                    String bads = pageRequest.filter.visit(propertiesExtractor).stream()
                            .filter(not(ALLOWED_CRITERIA::contains))
                            .collect(Collectors.joining(", "));
                    if (!bads.isBlank()) {
                        return Flux.error(new BadCriteriaFilter(
                                String.format("Filters not allowed without authentication [ %s ]", bads)));
                    } else {
                        return newsRepository.list(pageRequest).map(rn -> News.builder()
                                .raw(rn)
                                .state(State.NONE)
                                .build());
                    }
                });
    }

    @Override
    public Mono<News> get(String id) {
        return list(PageRequest.one(Criteria.property("id").eq(id))).next();
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
