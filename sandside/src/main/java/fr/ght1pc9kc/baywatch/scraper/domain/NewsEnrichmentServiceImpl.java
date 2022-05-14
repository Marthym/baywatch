package fr.ght1pc9kc.baywatch.scraper.domain;

import fr.ght1pc9kc.baywatch.common.api.exceptions.UnauthorizedException;
import fr.ght1pc9kc.baywatch.common.domain.Hasher;
import fr.ght1pc9kc.baywatch.scraper.api.NewsEnrichmentService;
import fr.ght1pc9kc.baywatch.scraper.api.NewsFilter;
import fr.ght1pc9kc.baywatch.security.api.AuthenticationFacade;
import fr.ght1pc9kc.baywatch.security.api.model.Role;
import fr.ght1pc9kc.baywatch.security.api.model.RoleUtils;
import fr.ght1pc9kc.baywatch.techwatch.api.SystemMaintenanceService;
import fr.ght1pc9kc.baywatch.techwatch.api.model.News;
import fr.ght1pc9kc.baywatch.techwatch.api.model.RawNews;
import fr.ght1pc9kc.baywatch.techwatch.api.model.State;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.jetbrains.annotations.VisibleForTesting;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.time.Clock;
import java.util.List;
import java.util.Set;

@RequiredArgsConstructor
public class NewsEnrichmentServiceImpl implements NewsEnrichmentService {
    private static final String OPERATION_NOT_PERMITTED = "Operation not permitted !";

    private final List<NewsFilter> newsFilters;
    private final AuthenticationFacade authFacade;
    private final SystemMaintenanceService systemMaintenanceService;

    @Setter(value = AccessLevel.PACKAGE, onMethod = @__({@VisibleForTesting}))
    private Clock clock = Clock.systemUTC();

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
    public Mono<News> applyNewsFilters(News news) {
        Mono<RawNews> raw = authFacade.getConnectedUser()
                .filter(u -> RoleUtils.hasRole(u.self, Role.USER))
                .switchIfEmpty(Mono.error(() -> new UnauthorizedException(OPERATION_NOT_PERMITTED)))
                .then(Mono.just(news.getRaw()));

        for (NewsFilter filter : newsFilters) {
            raw = raw.flatMap(filter::filter);
        }
        return raw.map(news::withRaw);
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
}
