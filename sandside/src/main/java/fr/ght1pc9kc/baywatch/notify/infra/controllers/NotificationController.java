package fr.ght1pc9kc.baywatch.notify.infra.controllers;

import fr.ght1pc9kc.baywatch.notify.api.NotifyManager;
import fr.ght1pc9kc.baywatch.notify.api.model.EventType;
import fr.ght1pc9kc.baywatch.notify.api.model.Severity;
import fr.ght1pc9kc.baywatch.notify.api.model.UserNotification;
import fr.ght1pc9kc.baywatch.security.api.AuthenticationFacade;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Slf4j
@RestController
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('USER', 'MANAGER', 'ADMIN')")
@RequestMapping("${baywatch.base-route}/sse")
public class NotificationController {
    private final NotifyManager notifyManager;
    private final AuthenticationFacade facade;

    @GetMapping(produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ServerSentEvent<?>> sse() {
        return Flux.create(notifyManager::subscribe);
    }

    @GetMapping("/test")
    @PreAuthorize("hasRole('ADMIN')")
    public Mono<Void> test(@RequestParam("msg") String msg) {
        return facade.getConnectedUser().map(user -> {
            notifyManager.broadcast(EventType.NEWS_UPDATE, "UPDATE " + msg);
            notifyManager.send(user.id(), EventType.USER_NOTIFICATION, UserNotification.builder()
                    .code(UserNotification.CODE_OK)
                    .severity(Severity.info)
                    .message("PERSO " + msg)
                    .delay(1000)
                    .build());
            return user;
        }).then();
    }
}
