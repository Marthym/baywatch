package fr.ght1pc9kc.baywatch.security.domain.subscribers;

import fr.ght1pc9kc.baywatch.common.api.model.TemplateVariable;
import fr.ght1pc9kc.baywatch.security.api.model.User;
import fr.ght1pc9kc.baywatch.security.domain.ports.MailSenderPort;
import fr.ght1pc9kc.baywatch.security.domain.ports.UserEventPublisherPort;
import fr.ght1pc9kc.entity.api.Entity;
import reactor.core.publisher.Mono;

import java.util.EnumMap;

public class WelcomeMailUserSubscriber {
    private final MailSenderPort mailSender;

    public WelcomeMailUserSubscriber(UserEventPublisherPort userPublisher,
                                     MailSenderPort mailSender) {
        this.mailSender = mailSender;
        userPublisher.events()
                .flatMap(this::onNewUser)
                .subscribe();
    }

    private Mono<Void> onNewUser(Entity<User> user) {
        EnumMap<TemplateVariable, String> variables = new EnumMap<>(TemplateVariable.class);
        variables.put(TemplateVariable.TOKEN, user.self().login());
        variables.put(TemplateVariable.USERNAME, user.self().login());
        variables.put(TemplateVariable.BASE_URL, user.self().login());
        return mailSender.send(MailSenderPort.MailTemplateType.WELCOME_USER, user.self().mail(), variables);
    }
}
