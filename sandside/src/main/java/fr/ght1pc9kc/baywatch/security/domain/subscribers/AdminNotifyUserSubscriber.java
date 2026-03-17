package fr.ght1pc9kc.baywatch.security.domain.subscribers;

import fr.ght1pc9kc.baywatch.security.api.AuthenticationFacade;
import fr.ght1pc9kc.baywatch.security.api.UserService;
import fr.ght1pc9kc.baywatch.security.api.model.Role;
import fr.ght1pc9kc.baywatch.security.api.model.User;
import fr.ght1pc9kc.baywatch.security.domain.ports.NotificationPort;
import fr.ght1pc9kc.baywatch.security.domain.ports.UserEventPublisherPort;
import fr.ght1pc9kc.entity.api.Entity;
import fr.ght1pc9kc.juery.api.Criteria;
import fr.ght1pc9kc.juery.api.PageRequest;
import reactor.core.Disposable;
import reactor.core.publisher.Mono;

import static fr.ght1pc9kc.baywatch.common.api.DefaultMeta.NO_ONE;
import static fr.ght1pc9kc.baywatch.common.api.model.EntitiesProperties.ROLES;
import static fr.ght1pc9kc.baywatch.common.api.model.UserMeta.createdBy;
import static fr.ght1pc9kc.baywatch.notify.api.model.EventType.USER_NOTIFICATION;
import static java.util.function.Predicate.not;

public class AdminNotifyUserSubscriber {
    private final UserService userService;
    private final NotificationPort notificationPort;
    private final Disposable disposable;

    public AdminNotifyUserSubscriber(UserEventPublisherPort userPublisher,
                                     UserService userService, NotificationPort notificationPort) {
        this.userService = userService;
        this.notificationPort = notificationPort;
        this.disposable = userPublisher.onEvent(this::onNewUser);
    }

    private Mono<Void> onNewUser(Entity<User> user) {
        return userService.list(PageRequest.all(Criteria.property(ROLES).eq(Role.ADMIN.toString())))
                .contextWrite(AuthenticationFacade.withSystemAuthentication())
                .filter(not(admin -> admin.id().equals(user.meta(createdBy).orElse(NO_ONE))))
                .map(admin -> notificationPort.send(admin.id(), USER_NOTIFICATION,
                        String.format("New user %s created by %s.", user.self().login(), user.meta(createdBy).orElse(NO_ONE))))
                .then();
    }

    public void destroy() {
        this.disposable.dispose();
    }
}
