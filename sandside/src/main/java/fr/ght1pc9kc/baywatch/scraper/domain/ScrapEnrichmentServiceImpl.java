package fr.ght1pc9kc.baywatch.scraper.domain;

import fr.ght1pc9kc.baywatch.common.api.exceptions.UnauthorizedException;
import fr.ght1pc9kc.baywatch.common.domain.Hasher;
import fr.ght1pc9kc.baywatch.common.domain.Try;
import fr.ght1pc9kc.baywatch.notify.api.NotifyService;
import fr.ght1pc9kc.baywatch.notify.api.model.EventType;
import fr.ght1pc9kc.baywatch.notify.api.model.Severity;
import fr.ght1pc9kc.baywatch.notify.api.model.UserNotification;
import fr.ght1pc9kc.baywatch.scraper.api.NewsFilter;
import fr.ght1pc9kc.baywatch.scraper.api.ScrapEnrichmentService;
import fr.ght1pc9kc.baywatch.scraper.api.model.AtomEntry;
import fr.ght1pc9kc.baywatch.scraper.api.model.AtomFeed;
import fr.ght1pc9kc.baywatch.scraper.domain.model.FeedsFilter;
import fr.ght1pc9kc.baywatch.scraper.domain.model.ex.NewsScrapingException;
import fr.ght1pc9kc.baywatch.security.api.AuthenticationFacade;
import fr.ght1pc9kc.baywatch.security.api.model.Role;
import fr.ght1pc9kc.baywatch.security.api.model.RoleUtils;
import fr.ght1pc9kc.baywatch.techwatch.api.SystemMaintenanceService;
import fr.ght1pc9kc.baywatch.techwatch.api.model.News;
import fr.ght1pc9kc.baywatch.techwatch.api.model.RawNews;
import fr.ght1pc9kc.baywatch.techwatch.api.model.State;
import fr.ght1pc9kc.juery.api.Criteria;
import fr.ght1pc9kc.juery.api.PageRequest;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.jetbrains.annotations.VisibleForTesting;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;

import java.net.URI;
import java.time.Clock;
import java.time.Duration;
import java.util.List;
import java.util.Set;

import static fr.ght1pc9kc.baywatch.common.api.model.EntitiesProperties.ID;
import static fr.ght1pc9kc.baywatch.common.api.model.EntitiesProperties.USER_ID;

@RequiredArgsConstructor
public class ScrapEnrichmentServiceImpl implements ScrapEnrichmentService {
    private static final String OPERATION_NOT_PERMITTED = "Operation not permitted !";

    private static final UserNotification DEFAULT_NOTIFICATION = UserNotification.builder()
            .code(UserNotification.CODE_NEWS_ADD)
            .severity(Severity.notice)
            .message("News successfully added !")
            .delay(Duration.ofSeconds(5).toMillis())
            .actions("VSC")
            .build();

    private final List<NewsFilter> newsFilters;
    private final List<FeedsFilter> feedsFilters;
    private final AuthenticationFacade authFacade;
    private final SystemMaintenanceService systemMaintenanceService;
    private final NotifyService notifyService;
    private final Scheduler scraperScheduler;

    @Setter(value = AccessLevel.PACKAGE, onMethod = @__({@VisibleForTesting}))
    private Clock clock = Clock.systemUTC();

    @Override
    public Mono<Void> scrapSingleNews(URI uri) {
        return authFacade.getConnectedUser()
                .transformDeferredContextual((original, context) -> original.doOnNext(user -> buildStandaloneNews(uri)
                        .flatMap(this::applyNewsFilters)
                        .flatMap(t -> Mono.fromCallable(t::get)
                                .filterWhen(this::notAlreadyExists)
                                .flatMap(this::saveAndShare)
                                .switchIfEmpty(Mono.fromCallable(t::get)))
                        .contextWrite(context)
                        .subscribeOn(scraperScheduler)
                        .subscribe(n -> notifyService.send(user.id, EventType.USER_NOTIFICATION,
                                        DEFAULT_NOTIFICATION.toBuilder()
                                                .title(n.title())
                                                .target(n.id()).build()),
                                t -> notifyService.send(user.id, EventType.USER_NOTIFICATION,
                                        UserNotification.error(t.getLocalizedMessage())))
                ))
                .then();
    }

    @Override
    public Mono<News> buildStandaloneNews(URI link) {
        return authFacade.getConnectedUser()
                .filter(u -> RoleUtils.hasRole(u.self, Role.USER))
                .switchIfEmpty(Mono.error(() -> new UnauthorizedException(OPERATION_NOT_PERMITTED)))
                .map(u -> News.builder()
                        .raw(RawNews.builder()
                                .id(Hasher.identify(link))
                                .link(link)
                                .publication(clock.instant())
                                .build())
                        .feeds(Set.of(u.id))
                        .state(State.NONE)
                        .build());
    }

    @Override
    public Mono<Try<News>> applyNewsFilters(News news) {
        Mono<RawNews> raw = authFacade.getConnectedUser()
                .filter(u -> RoleUtils.hasRole(u.self, Role.USER))
                .switchIfEmpty(Mono.error(() -> new UnauthorizedException(OPERATION_NOT_PERMITTED)))
                .then(Mono.just(news.getRaw()));

        for (NewsFilter filter : newsFilters) {
            raw = raw.flatMap(filter::filter);
        }
        return raw.map(Try.of(news::withRaw))
                .onErrorResume(e -> {
                    AtomEntry atomEntry = new AtomEntry(
                            news.id(), news.title(), news.image(), news.description(), news.publication(),
                            news.link(), news.getFeeds());
                    return Mono.just(Try.fail(new NewsScrapingException(atomEntry, e)));
                });
    }

    private Mono<Boolean> notAlreadyExists(News news) {
        return authFacade.getConnectedUser()
                .filter(u -> RoleUtils.hasRole(u.self, Role.USER))
                .switchIfEmpty(Mono.error(() -> new UnauthorizedException(OPERATION_NOT_PERMITTED)))

                .flatMapMany(u ->
                        systemMaintenanceService.newsList(PageRequest.one(Criteria.property(ID).eq(news.id())))
                                .map(News::getFeeds)
                                .flatMap(feeds -> systemMaintenanceService.feedList(PageRequest.all(Criteria.property(ID).in(feeds)
                                        .and(Criteria.property(USER_ID).eq(u.id)))))
                                .contextWrite(AuthenticationFacade.withSystemAuthentication()))

                .hasElements().map(b -> !b);
    }

    @Override
    public Mono<News> saveAndShare(News news) {
        return authFacade.getConnectedUser()
                .filter(u -> RoleUtils.hasRole(u.self, Role.USER))
                .switchIfEmpty(Mono.error(() -> new UnauthorizedException(OPERATION_NOT_PERMITTED)))

                .flatMap(u ->
                        systemMaintenanceService.newsLoad(List.of(news))
                                .contextWrite(AuthenticationFacade.withSystemAuthentication())
                )

                .filter(persisted -> persisted == 1)
                .switchIfEmpty(Mono.error(() -> new IllegalArgumentException("Unable to persist news !")))
                .then(Mono.just(news));
    }

    @Override
    public Mono<AtomFeed> applyFeedsFilters(AtomFeed feed) {
        Mono<AtomFeed> raw = authFacade.getConnectedUser()
                .filter(u -> RoleUtils.hasRole(u.self, Role.USER))
                .switchIfEmpty(Mono.error(() -> new UnauthorizedException(OPERATION_NOT_PERMITTED)))
                .then(Mono.just(feed));

        for (FeedsFilter filter : feedsFilters) {
            raw = raw.flatMap(filter::filter);
        }
        return raw;
    }
}
