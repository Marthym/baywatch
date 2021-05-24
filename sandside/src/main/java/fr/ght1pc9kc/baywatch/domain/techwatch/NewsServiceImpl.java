package fr.ght1pc9kc.baywatch.domain.techwatch;

import fr.ght1pc9kc.baywatch.api.NewsService;
import fr.ght1pc9kc.baywatch.api.model.News;
import fr.ght1pc9kc.baywatch.api.security.model.Role;
import fr.ght1pc9kc.baywatch.domain.exceptions.BadCriteriaFilter;
import fr.ght1pc9kc.baywatch.domain.exceptions.UnauthenticatedUser;
import fr.ght1pc9kc.baywatch.domain.exceptions.UnauthorizedOperation;
import fr.ght1pc9kc.baywatch.domain.ports.AuthenticationFacade;
import fr.ght1pc9kc.baywatch.domain.techwatch.model.QueryContext;
import fr.ght1pc9kc.baywatch.domain.techwatch.ports.NewsPersistencePort;
import fr.ght1pc9kc.juery.api.Criteria;
import fr.ght1pc9kc.juery.api.PageRequest;
import lombok.AllArgsConstructor;
import org.jetbrains.annotations.Nullable;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static fr.ght1pc9kc.baywatch.api.model.EntitiesProperties.*;
import static java.util.function.Predicate.not;

@Service
@AllArgsConstructor
public class NewsServiceImpl implements NewsService {
    private static final Set<String> ALLOWED_CRITERIA = Set.of(ID, PUBLICATION, SHARED, STATE, TITLE, FEED_ID);
    private static final Set<String> ALLOWED_AUTHENTICATED_CRITERIA = Set.of(READ);

    private final Criteria.Visitor<List<String>> propertiesExtractor;
    private final NewsPersistencePort newsRepository;
    private final AuthenticationFacade authFacade;

    @Override
    public Flux<News> list(PageRequest pageRequest) {
        return authFacade.getConnectedUser()
                .switchIfEmpty(Mono.error(new UnauthenticatedUser("Authentication not found !")))
                .map(user -> throwOnInvalidRequestFilters(pageRequest, user))
                .map(user -> QueryContext.from(pageRequest).withUserId(user.id))
                .onErrorResume(UnauthenticatedUser.class, (e) ->
                        Mono.fromCallable(() -> throwOnInvalidRequestFilters(pageRequest, null))
                                .thenReturn(QueryContext.from(pageRequest)))
                .flatMapMany(newsRepository::list);
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

    @Override
    public Mono<Integer> orphanize(Collection<String> toOrphanize) {
        return authFacade.getConnectedUser()
                .filter(user -> Role.SYSTEM == user.role)
                .switchIfEmpty(Mono.error(new UnauthorizedOperation("Orphanize news not permitted for user !")))
                .flatMap(user -> newsRepository.deleteFeedLink(toOrphanize));
    }

    @Override
    public Mono<Integer> delete(Collection<String> toDelete) {
        return authFacade.getConnectedUser()
                .filter(user -> Role.SYSTEM == user.role)
                .switchIfEmpty(Mono.error(new UnauthorizedOperation("Deleting news not permitted for user !")))
                .flatMap(user -> newsRepository.delete(toDelete));
    }

    private <T> T throwOnInvalidRequestFilters(PageRequest request, @Nullable T ignore) {
        Stream<String> stream = request.filter().visit(propertiesExtractor).stream()
                .filter(not(ALLOWED_CRITERIA::contains));
        Stream<String> authStream = (ignore != null)
                ? stream.filter(not(ALLOWED_AUTHENTICATED_CRITERIA::contains))
                : stream;
        String bads = authStream.collect(Collectors.joining(", "));
        if (!bads.isBlank()) {
            throw new BadCriteriaFilter(String.format("Filters not allowed [ %s ]", bads));
        } else {
            return ignore;
        }
    }
}
