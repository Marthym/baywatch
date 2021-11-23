package fr.ght1pc9kc.baywatch.infra.notify;

import fr.ght1pc9kc.baywatch.api.notify.NotifyService;
import fr.ght1pc9kc.baywatch.domain.notify.NotifyServiceImpl;
import lombok.experimental.Delegate;
import org.springframework.stereotype.Service;

@Service
public class NotifyServiceAdapter implements NotifyService {
    @Delegate
    private final NotifyService delegate;

    public NotifyServiceAdapter() {
        this.delegate = new NotifyServiceImpl();
    }
}
