package fr.ght1pc9kc.baywatch.notify.infra.persistence;

import fr.ght1pc9kc.baywatch.common.infra.DatabaseQualifier;
import fr.ght1pc9kc.baywatch.notify.api.model.MailTemplateName;
import fr.ght1pc9kc.baywatch.notify.domain.model.MailTemplate;
import fr.ght1pc9kc.baywatch.notify.domain.model.TranslatedTemplate;
import fr.ght1pc9kc.baywatch.notify.domain.ports.MailTemplatePersistencePort;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.core.io.buffer.DefaultDataBufferFactory;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class MailTemplatePersistenceAdapter implements MailTemplatePersistencePort {
    private static final List<MailTemplate> SUPPORTED_TEMPLATES = List.of(
            new MailTemplate(MailTemplateName.PASSWORD_RESET, Locale.ENGLISH, "Baywatch - Request for password reset"),
            new MailTemplate(MailTemplateName.PASSWORD_RESET, Locale.FRANCE, "Baywatch - Request for password reset"),
            new MailTemplate(MailTemplateName.WELCOME_USER, Locale.ENGLISH, "Baywatch - Welcome to Baywatch"),
            new MailTemplate(MailTemplateName.WELCOME_USER, Locale.FRANCE, "Baywatch - Bienvenue dans Baywatch")
    );

    private final @DatabaseQualifier Scheduler scheduler;

    @Override
    public Mono<TranslatedTemplate> get(MailTemplateName name, Locale locale) {
        Optional<MailTemplate> mailTemplate = SUPPORTED_TEMPLATES.stream()
                .filter(t -> t.name().equals(name) && t.locale().equals(locale))
                .findFirst();
        if (mailTemplate.isEmpty()) {
            return Mono.error(new IllegalArgumentException("Mail template not found: " + name + " " + locale));
        }
        String classpathLocation = String.format("mails/%s-%s.html", name.name().toLowerCase(), locale.toLanguageTag());
        Resource resource = new ClassPathResource(classpathLocation);
        if (!resource.exists()) {
            return Mono.error(new IllegalArgumentException("Mail template file not found: " + classpathLocation));
        }
        return DataBufferUtils.join(DataBufferUtils.read(resource, new DefaultDataBufferFactory(), 4096))
                .map(dataBuffer -> {
                    byte[] bytes = new byte[dataBuffer.readableByteCount()];
                    dataBuffer.read(bytes);
                    DataBufferUtils.release(dataBuffer);
                    return new String(bytes, StandardCharsets.UTF_8);
                })
                .subscribeOn(scheduler)
                .map(body -> new TranslatedTemplate(mailTemplate.get().name(), mailTemplate.get().subject(), body));
    }
}
