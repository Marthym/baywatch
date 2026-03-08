package fr.ght1pc9kc.baywatch.security.domain.subscribers;

import fr.ght1pc9kc.baywatch.common.api.model.TemplateVariable;
import fr.ght1pc9kc.baywatch.security.api.model.User;
import fr.ght1pc9kc.baywatch.security.domain.ports.MailSenderPort;
import fr.ght1pc9kc.baywatch.security.domain.ports.UserEventPublisherPort;
import fr.ght1pc9kc.entity.api.Entity;
import lombok.extern.slf4j.Slf4j;
import reactor.core.Disposable;
import reactor.core.publisher.Mono;

import java.util.EnumMap;

@Slf4j
public class WelcomeMailUserSubscriber {
    private final MailSenderPort mailSender;
    private final Disposable disposable;

    public WelcomeMailUserSubscriber(UserEventPublisherPort userPublisher, MailSenderPort mailSender) {
        this.mailSender = mailSender;
        this.disposable = userPublisher.onEvent(this::onNewUser);
        log.debug("WelcomeMailUserSubscriber started.");
    }

    private Mono<Void> onNewUser(Entity<User> user) {
        EnumMap<TemplateVariable, String> variables = new EnumMap<>(TemplateVariable.class);
        variables.put(TemplateVariable.TOKEN, user.self().login());
        variables.put(TemplateVariable.USERNAME, user.self().login());
        variables.put(TemplateVariable.BASE_URL, user.self().login());

        return mailSender.send(MailSenderPort.MailTemplateType.WELCOME_USER, user.self().mail(), variables);
    }

    public void destroy() {
        this.disposable.dispose();
    }
}
