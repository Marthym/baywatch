package fr.ght1pc9kc.baywatch.notify.infra;

import fr.ght1pc9kc.baywatch.notify.api.NotifyManager;
import fr.ght1pc9kc.baywatch.notify.api.NotifyService;
import fr.ght1pc9kc.baywatch.notify.domain.NotifyServiceImpl;
import fr.ght1pc9kc.baywatch.security.api.AuthenticationFacade;
import lombok.experimental.Delegate;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.PreDestroy;

@Slf4j
@Service
public class NotifyServiceAdapter implements NotifyService, NotifyManager {
    @Delegate
    private final NotifyServiceImpl delegate;

    public NotifyServiceAdapter(AuthenticationFacade authFacade) {
        this.delegate = new NotifyServiceImpl(authFacade);
    }

    @PreDestroy
    private void preDestroy() {
        delegate.close();
        log.info("All Notifications Session complete gracefully.");
    }
}
