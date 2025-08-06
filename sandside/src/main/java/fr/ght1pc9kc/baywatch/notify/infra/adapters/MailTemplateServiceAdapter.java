package fr.ght1pc9kc.baywatch.notify.infra.adapters;

import fr.ght1pc9kc.baywatch.notify.domain.MailTemplateService;
import fr.ght1pc9kc.baywatch.notify.domain.ports.MailTemplatePersistencePort;
import fr.ght1pc9kc.baywatch.notify.domain.services.MailTemplateServiceImpl;
import lombok.experimental.Delegate;
import org.springframework.stereotype.Service;

@Service
public class MailTemplateServiceAdapter implements MailTemplateService {
    @Delegate
    private final MailTemplateService delegate;

    public MailTemplateServiceAdapter(MailTemplatePersistencePort persistencePort) {
        this.delegate = new MailTemplateServiceImpl(persistencePort);
    }
}
