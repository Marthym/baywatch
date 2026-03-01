package fr.ght1pc9kc.baywatch.security.domain.subscribers;

import fr.ght1pc9kc.baywatch.common.api.model.TemplateVariable;
import fr.ght1pc9kc.baywatch.common.api.model.UserMeta;
import fr.ght1pc9kc.baywatch.security.api.model.User;
import fr.ght1pc9kc.baywatch.security.domain.ports.ReactiveContextPort;
import fr.ght1pc9kc.baywatch.security.domain.ports.MailSenderPort;
import fr.ght1pc9kc.baywatch.security.domain.ports.UserEventPublisherPort;
import fr.ght1pc9kc.entity.api.Entity;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

import java.net.InetSocketAddress;
import java.net.URI;
import java.util.EnumMap;

@Slf4j
public class WelcomeMailUserSubscriber {
    private final MailSenderPort mailSender;
    private final ReactiveContextPort clientInfo;

    public WelcomeMailUserSubscriber(UserEventPublisherPort userPublisher, MailSenderPort mailSender, ReactiveContextPort clientInfo) {
        this.mailSender = mailSender;
        this.clientInfo = clientInfo;
        userPublisher.events()
                .flatMap(this::onNewUser)
                .onErrorResume(t -> {
                    log.error("Failed to send welcome mail ! -> {}: {}", t.getClass(), t.getLocalizedMessage());
                    log.debug("STACKTRACE", t);
                    return Mono.empty();
                }).subscribe();
        log.debug("WelcomeMailUserSubscriber started.");
    }

    private Mono<Void> onNewUser(Entity<User> user) {
        EnumMap<TemplateVariable, String> variables = new EnumMap<>(TemplateVariable.class);
        variables.put(TemplateVariable.TOKEN, user.self().login());
        variables.put(TemplateVariable.USERNAME, user.self().login());
        variables.put(TemplateVariable.BASE_URL, user.self().login());

        return mailSender.send(MailSenderPort.MailTemplateType.WELCOME_USER, user.self().mail(), variables)
                .contextWrite(clientInfo.withUserContext(user));
    }
}
