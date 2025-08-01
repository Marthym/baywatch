package fr.ght1pc9kc.baywatch.security.infra.adapters;

import fr.ght1pc9kc.baywatch.admin.api.FeedManagementService;
import fr.ght1pc9kc.baywatch.common.api.model.FeedMeta;
import fr.ght1pc9kc.baywatch.security.api.model.User;
import fr.ght1pc9kc.baywatch.security.domain.model.PersonalFeed;
import fr.ght1pc9kc.baywatch.security.domain.ports.TechwatchModulePort;
import fr.ght1pc9kc.baywatch.techwatch.api.FeedService;
import fr.ght1pc9kc.baywatch.techwatch.api.model.WebFeed;
import fr.ght1pc9kc.entity.api.Entity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.List;

@Component
@RequiredArgsConstructor
public class TechwatchModuleAdapter implements TechwatchModulePort {
    private final FeedService feedService;
    private final FeedManagementService feedManagementService;

    @Override
    public Mono<Void> addAndSubscribePersonalFeed(PersonalFeed personalFeed) {
        Entity<WebFeed> personalUserWebFeed = Entity.identify(WebFeed.builder()
                        .name(personalFeed.name())
                        .description(personalFeed.description())
                        .icon(personalFeed.icon())
                        .location(personalFeed.location())
                        .build())
                .meta(FeedMeta.visible, false)
                .withId(personalFeed.id());

        return feedService.addAndSubscribe(List.of(personalUserWebFeed))
                .then();
    }

    @Override
    public Mono<Void> unsubscribePersonalFeed(Entity<User> user) {
        return feedService.unsubscribe(List.of(user.id())).then();
    }

    @Override
    public Mono<Void> deletePersonalFeed(Entity<User> user) {
        return feedManagementService.delete(List.of(user.id()));
    }
}
