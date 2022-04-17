package fr.ght1pc9kc.baywatch.techwatch.domain;

import fr.ght1pc9kc.baywatch.common.api.model.Entity;
import fr.ght1pc9kc.baywatch.common.domain.exceptions.BadRequestCriteria;
import fr.ght1pc9kc.baywatch.security.api.AuthenticationFacade;
import fr.ght1pc9kc.baywatch.security.api.model.Role;
import fr.ght1pc9kc.baywatch.security.domain.exceptions.UnauthenticatedUser;
import fr.ght1pc9kc.baywatch.security.domain.exceptions.UnauthorizedOperation;
import fr.ght1pc9kc.baywatch.techwatch.api.NewsService;
import fr.ght1pc9kc.baywatch.techwatch.api.model.Feed;
import fr.ght1pc9kc.baywatch.techwatch.api.model.News;
import fr.ght1pc9kc.baywatch.techwatch.api.model.State;
import fr.ght1pc9kc.baywatch.techwatch.domain.filter.CriteriaModifierVisitor;
import fr.ght1pc9kc.baywatch.techwatch.domain.model.QueryContext;
import fr.ght1pc9kc.baywatch.techwatch.domain.ports.FeedPersistencePort;
import fr.ght1pc9kc.baywatch.techwatch.domain.ports.NewsPersistencePort;
import fr.ght1pc9kc.baywatch.techwatch.domain.ports.StatePersistencePort;
import fr.ght1pc9kc.juery.api.Criteria;
import fr.ght1pc9kc.juery.api.PageRequest;
import fr.ght1pc9kc.juery.api.Pagination;
import fr.ght1pc9kc.juery.api.filter.CriteriaVisitor;
import fr.ght1pc9kc.juery.basic.filter.ListPropertiesCriteriaVisitor;
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

import static fr.ght1pc9kc.baywatch.common.api.model.EntitiesProperties.FEED_ID;
import static fr.ght1pc9kc.baywatch.common.api.model.EntitiesProperties.ID;
import static fr.ght1pc9kc.baywatch.common.api.model.EntitiesProperties.NEWS_ID;
import static fr.ght1pc9kc.baywatch.common.api.model.EntitiesProperties.PUBLICATION;
import static fr.ght1pc9kc.baywatch.common.api.model.EntitiesProperties.READ;
import static fr.ght1pc9kc.baywatch.common.api.model.EntitiesProperties.SHARED;
import static fr.ght1pc9kc.baywatch.common.api.model.EntitiesProperties.STATE;
import static fr.ght1pc9kc.baywatch.common.api.model.EntitiesProperties.TAGS;
import static fr.ght1pc9kc.baywatch.common.api.model.EntitiesProperties.TITLE;
import static fr.ght1pc9kc.baywatch.common.api.model.EntitiesProperties.USER_ID;
import static java.util.function.Predicate.not;

@Service
@AllArgsConstructor
public class NewsServiceImpl implements NewsService {
    private static final Set<String> ALLOWED_CRITERIA = Set.of(ID, STATE, TITLE, FEED_ID);
    private static final Set<String> ALLOWED_AUTHENTICATED_CRITERIA = Set.of(READ, SHARED, TAGS, PUBLICATION);
    private static final int MAX_ANONYMOUS_NEWS = 20;
    private static final String AUTHENTICATION_NOT_FOUND = "Authentication not found !";

    private final CriteriaVisitor<List<String>> propertiesExtractor;
    private final NewsPersistencePort newsRepository;
    private final FeedPersistencePort feedRepository;
    private final StatePersistencePort stateRepository;
    private final AuthenticationFacade authFacade;

    @Override
    public Flux<News> list(PageRequest pageRequest) {
        PageRequest validRequest = pageRequest.withFilter(pageRequest.filter().accept(new CriteriaModifierVisitor()));
        return authFacade.getConnectedUser()
                .switchIfEmpty(Mono.error(() -> new UnauthenticatedUser(AUTHENTICATION_NOT_FOUND)))
                .map(user -> throwOnInvalidRequest(validRequest, user))
                .map(user -> QueryContext.from(validRequest).withUserId(user.id))
                .flatMap(this::forgeAggregateQueryContext)
                .onErrorResume(UnauthenticatedUser.class, e ->
                        Mono.fromCallable(() -> throwOnInvalidRequest(validRequest, null))
                                .thenReturn(QueryContext.from(validRequest)))
                .flatMapMany(newsRepository::list);
    }

    @Override
    public Mono<Integer> count(PageRequest pageRequest) {
        PageRequest validRequest = pageRequest.withFilter(pageRequest.filter().accept(new CriteriaModifierVisitor()));
        return authFacade.getConnectedUser()
                .switchIfEmpty(Mono.error(() -> new UnauthenticatedUser(AUTHENTICATION_NOT_FOUND)))
                .map(user -> throwOnInvalidRequest(validRequest, user))
                .map(user -> QueryContext.all(validRequest.filter()).withUserId(user.id))
                .flatMap(this::forgeAggregateQueryContext)
                .flatMap(newsRepository::count)
                .onErrorResume(UnauthenticatedUser.class, e -> Mono.just(pageRequest.pagination().size()));
    }

    public Mono<QueryContext> forgeAggregateQueryContext(QueryContext qCtx) {
        List<String> props = qCtx.filter.accept(new ListPropertiesCriteriaVisitor());
        if (props.size() == 1 && ID.equals(props.get(0))) {
            // Shortcut for get one News from id
            return Mono.just(qCtx);
        }
        Mono<List<String>> shared = getStateQueryContext(qCtx);
        Mono<List<String>> feeds = getFeedFor(qCtx);

        return Mono.zip(shared, feeds).map(contexts -> {
            Criteria filters = Criteria.property(FEED_ID).in(contexts.getT2());
            if (!contexts.getT1().isEmpty()) {
                filters = Criteria.or(filters, Criteria.property(NEWS_ID).in(contexts.getT1()));
            }
            filters = Criteria.and(filters, qCtx.getFilter());

            return QueryContext.builder()
                    .pagination(qCtx.getPagination())
                    .userId(qCtx.getUserId())
                    .filter(filters)
                    .build();
        });
    }

    public Mono<List<String>> getFeedFor(QueryContext qCtx) {
        return feedRepository.list(qCtx)
                .map(Feed::getId)
                .collectList();
    }

    public Mono<List<String>> getStateQueryContext(QueryContext qCtx) {
        Criteria sharedCriteria = Criteria.not(Criteria.property(USER_ID).eq(qCtx.userId))
                .and(Criteria.property(SHARED).eq(true));

        QueryContext stateQueryContext = QueryContext.builder()
                .filter(sharedCriteria)
                .pagination(Pagination.ALL)
                .build();

        return stateRepository.list(stateQueryContext)
                .map(state -> state.id)
                .distinct().collectList();
    }

    @Override
    public Mono<News> get(String id) {
        return list(PageRequest.one(Criteria.property(ID).eq(id))).next();
    }

    @Override
    public Mono<Entity<State>> mark(String id, int flag) {
        return authFacade.getConnectedUser()
                .switchIfEmpty(Mono.error(() -> new UnauthenticatedUser(AUTHENTICATION_NOT_FOUND)))
                .flatMap(user -> stateRepository.flag(id, user.id, flag));
    }

    @Override
    public Mono<Entity<State>> unmark(String id, int flag) {
        return authFacade.getConnectedUser()
                .switchIfEmpty(Mono.error(() -> new UnauthenticatedUser(AUTHENTICATION_NOT_FOUND)))
                .flatMap(user -> stateRepository.unflag(id, user.id, flag));
    }

    @Override
    public Mono<Integer> orphanize(Collection<String> toOrphanize) {
        return authFacade.getConnectedUser()
                .filter(user -> Role.SYSTEM == user.self.role)
                .switchIfEmpty(Mono.error(() -> new UnauthorizedOperation("Orphanize news not permitted for user !")))
                .flatMap(user -> newsRepository.unlink(toOrphanize));
    }

    @Override
    public Mono<Integer> delete(Collection<String> toDelete) {
        return authFacade.getConnectedUser()
                .filter(user -> Role.SYSTEM == user.self.role)
                .switchIfEmpty(Mono.error(() -> new UnauthorizedOperation("Deleting news not permitted for user !")))
                .flatMap(user -> newsRepository.delete(toDelete));
    }

    private <T> T throwOnInvalidRequest(PageRequest request, @Nullable T ignore) {
        Stream<String> stream = request.filter().accept(propertiesExtractor).stream()
                .filter(not(ALLOWED_CRITERIA::contains));
        Stream<String> authStream = (ignore != null)
                ? stream.filter(not(ALLOWED_AUTHENTICATED_CRITERIA::contains))
                : stream;
        String bads = authStream.collect(Collectors.joining(", "));

        Pagination pagination = request.pagination();
        boolean isPaginationForAnonymous = ignore == null && ((pagination.offset() - 1) + pagination.size() > MAX_ANONYMOUS_NEWS);
        if (!bads.isBlank()) {
            throw new BadRequestCriteria(String.format("Filters not allowed [ %s ]", bads));
        }
        if (isPaginationForAnonymous) {
            throw new BadRequestCriteria("Pagination not allowed for anonymous !");
        }
        return ignore;
    }
}
