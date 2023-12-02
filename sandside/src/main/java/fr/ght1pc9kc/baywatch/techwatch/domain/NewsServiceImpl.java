package fr.ght1pc9kc.baywatch.techwatch.domain;

import fr.ght1pc9kc.baywatch.common.api.model.Entity;
import fr.ght1pc9kc.baywatch.common.domain.exceptions.BadRequestCriteria;
import fr.ght1pc9kc.baywatch.security.api.AuthenticationFacade;
import fr.ght1pc9kc.baywatch.security.api.model.Role;
import fr.ght1pc9kc.baywatch.security.api.model.RoleUtils;
import fr.ght1pc9kc.baywatch.security.domain.exceptions.UnauthenticatedUser;
import fr.ght1pc9kc.baywatch.security.domain.exceptions.UnauthorizedOperation;
import fr.ght1pc9kc.baywatch.techwatch.api.NewsService;
import fr.ght1pc9kc.baywatch.techwatch.api.model.News;
import fr.ght1pc9kc.baywatch.techwatch.api.model.State;
import fr.ght1pc9kc.baywatch.techwatch.api.model.WebFeed;
import fr.ght1pc9kc.baywatch.techwatch.domain.filter.CriteriaModifierVisitor;
import fr.ght1pc9kc.baywatch.techwatch.domain.model.QueryContext;
import fr.ght1pc9kc.baywatch.techwatch.domain.ports.FeedPersistencePort;
import fr.ght1pc9kc.baywatch.techwatch.domain.ports.NewsPersistencePort;
import fr.ght1pc9kc.baywatch.techwatch.domain.ports.StatePersistencePort;
import fr.ght1pc9kc.baywatch.techwatch.domain.ports.TeamServicePort;
import fr.ght1pc9kc.juery.api.Criteria;
import fr.ght1pc9kc.juery.api.PageRequest;
import fr.ght1pc9kc.juery.api.Pagination;
import fr.ght1pc9kc.juery.api.filter.CriteriaVisitor;
import fr.ght1pc9kc.juery.basic.filter.ListPropertiesCriteriaVisitor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.Nullable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static fr.ght1pc9kc.baywatch.common.api.model.EntitiesProperties.FEED_ID;
import static fr.ght1pc9kc.baywatch.common.api.model.EntitiesProperties.ID;
import static fr.ght1pc9kc.baywatch.common.api.model.EntitiesProperties.KEEP;
import static fr.ght1pc9kc.baywatch.common.api.model.EntitiesProperties.NEWS_ID;
import static fr.ght1pc9kc.baywatch.common.api.model.EntitiesProperties.POPULAR;
import static fr.ght1pc9kc.baywatch.common.api.model.EntitiesProperties.PUBLICATION;
import static fr.ght1pc9kc.baywatch.common.api.model.EntitiesProperties.READ;
import static fr.ght1pc9kc.baywatch.common.api.model.EntitiesProperties.SHARED;
import static fr.ght1pc9kc.baywatch.common.api.model.EntitiesProperties.STATE;
import static fr.ght1pc9kc.baywatch.common.api.model.EntitiesProperties.TAGS;
import static fr.ght1pc9kc.baywatch.common.api.model.EntitiesProperties.TITLE;
import static fr.ght1pc9kc.baywatch.common.api.model.EntitiesProperties.USER_ID;
import static java.util.function.Predicate.not;

@Slf4j
@RequiredArgsConstructor
public class NewsServiceImpl implements NewsService {
    private static final Set<String> ALLOWED_CRITERIA = Set.of(ID, STATE, TITLE, FEED_ID);
    private static final Set<String> ALLOWED_AUTHENTICATED_CRITERIA = Set.of(POPULAR, READ, SHARED, KEEP, TAGS, PUBLICATION);
    private static final int MAX_ANONYMOUS_NEWS = 20;
    private static final String AUTHENTICATION_NOT_FOUND = "Authentication not found !";

    private final CriteriaVisitor<List<String>> propertiesExtractor;
    private final NewsPersistencePort newsRepository;
    private final FeedPersistencePort feedRepository;
    private final StatePersistencePort stateRepository;
    private final AuthenticationFacade authFacade;
    private final TeamServicePort teamServicePort;

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
        return teamServicePort.getTeamMates(qCtx.getUserId())
                .concatWith(Mono.just(qCtx.getUserId()))
                .distinct()
                .collectList()
                .flatMap(teamMates -> {
                    Mono<List<String>> states = getStateQueryContext(teamMates);
                    Mono<List<String>> feeds = getFeedFor(qCtx, props);

                    return Mono.zip(states, feeds).map(contexts -> {
                        Criteria filters = Criteria.property(FEED_ID).in(contexts.getT2());
                        if (!contexts.getT1().isEmpty()) {
                            filters = Criteria.or(filters, Criteria.property(NEWS_ID).in(contexts.getT1()));
                        }
                        filters = Criteria.and(filters, qCtx.getFilter());

                        return QueryContext.builder()
                                .pagination(qCtx.getPagination())
                                .userId(qCtx.getUserId())
                                .teamMates(teamMates)
                                .filter(filters)
                                .build();
                    });
                });
    }

    /**
     * <p>Return the list of {@link WebFeed#reference()} corresponding to the {@link QueryContext}.</p>
     * <p>The User ID was concat to the list because User ID was the ID of the Feed containing orphan News</p>
     * <p>{@link Pagination} was removed from {@link QueryContext} because it must only impact the main query, Feed
     * query must not be impacted by the offset</p>
     *
     * @param qCtx  The query context
     * @param props The list of properties used in query context
     * @return The list of Feed IDs
     */
    public Mono<List<String>> getFeedFor(QueryContext qCtx, List<String> props) {
        QueryContext feedQCtx = (props.contains(FEED_ID))
                ? QueryContext.all(qCtx.filter)
                : QueryContext.all(qCtx.filter).withUserId(qCtx.userId);
        return feedRepository.list(feedQCtx)
                .map(f -> f.id)
                .collectList()
                .map(feeds -> {
                    if (feedQCtx.isScoped()) {
                        feeds.add(feedQCtx.getUserId());
                    }
                    return feeds;
                });
    }

    public Mono<List<String>> getStateQueryContext(List<String> teamMates) {

        if (teamMates.isEmpty()) {
            return Mono.empty();
        }
        Criteria sharedCriteria = Criteria.property(USER_ID).in(teamMates)
                .and(Criteria.property(SHARED).eq(true));

        QueryContext query = QueryContext.builder()
                .filter(sharedCriteria)
                .pagination(Pagination.ALL)
                .build();
        return stateRepository.list(query)
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
    public Mono<Integer> delete(Collection<String> toDelete) {
        return authFacade.getConnectedUser()
                .filter(user -> RoleUtils.hasRole(user.self, Role.SYSTEM))
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
